package com.happytails.social;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Locale;

/**
 * Small idempotent compatibility migration for existing Happy Tails databases.
 *
 * Hibernate still owns normal schema creation through ddl-auto=update. This
 * runner only repairs columns created by older Happy Tails builds where the
 * deployed database may have narrower or incomplete schemas.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseCompatibilityMigration implements ApplicationRunner {
  private static final Logger log=LoggerFactory.getLogger(DatabaseCompatibilityMigration.class);
  private final JdbcTemplate jdbc;
  private final DataSource dataSource;

  public DatabaseCompatibilityMigration(JdbcTemplate jdbc,DataSource dataSource){this.jdbc=jdbc;this.dataSource=dataSource;}

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if(!tableExists("pet_profiles")){
      log.info("Skipping compatibility migration because pet_profiles does not exist yet.");
      return;
    }

    add("adoption_date","date");
    add("gender","varchar(255)");
    add("personality","varchar(255)");
    add("favorite_activities","varchar(255)");
    add("pet_preferences","varchar(255)");
    add("private_account","boolean default false");
    add("show_location","boolean default true");
    add("message_permission","varchar(255) default 'EVERYONE'");
    add("play_date_permission","varchar(255) default 'EVERYONE'");

    // Older PostgreSQL deployments used varchar(255) for image/data URL fields.
    // Profile photos and activity/memory images use compressed data URLs, so
    // those columns must be widened for existing databases as well as new ones.
    if(databaseProduct().contains("postgresql")){
      jdbc.execute("alter table pet_profiles alter column avatar_url type text");
      if(tableExists("social_posts")&&columnExists("social_posts","media_url"))
        jdbc.execute("alter table social_posts alter column media_url type text");
      if(tableExists("pet_memories")&&columnExists("pet_memories","media_url"))
        jdbc.execute("alter table pet_memories alter column media_url type text");
    }

    jdbc.update("update pet_profiles set private_account=false where private_account is null");
    jdbc.update("update pet_profiles set show_location=true where show_location is null");
    jdbc.update("update pet_profiles set message_permission='EVERYONE' where message_permission is null or trim(message_permission)=''");
    jdbc.update("update pet_profiles set play_date_permission='EVERYONE' where play_date_permission is null or trim(play_date_permission)=''");
  }

  private void add(String column,String definition){
    if(columnExists("pet_profiles",column))return;
    jdbc.execute("alter table pet_profiles add column "+column+" "+definition);
  }

  private boolean tableExists(String table){
    try(Connection c=dataSource.getConnection()){
      DatabaseMetaData md=c.getMetaData();
      try(ResultSet rs=md.getTables(null,null,null,new String[]{"TABLE"})){
        while(rs.next())if(table.equalsIgnoreCase(rs.getString("TABLE_NAME")))return true;
      }
      return false;
    }catch(Exception e){throw new IllegalStateException("Could not inspect database schema before startup migration.",e);}
  }

  private boolean columnExists(String table,String column){
    try(Connection c=dataSource.getConnection()){
      DatabaseMetaData md=c.getMetaData();
      try(ResultSet rs=md.getColumns(null,null,null,null)){
        while(rs.next())if(table.equalsIgnoreCase(rs.getString("TABLE_NAME"))&&column.equalsIgnoreCase(rs.getString("COLUMN_NAME")))return true;
      }
      return false;
    }catch(Exception e){throw new IllegalStateException("Could not inspect database columns before startup migration.",e);}
  }

  private String databaseProduct(){
    try(Connection c=dataSource.getConnection()){
      return c.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
    }catch(Exception e){throw new IllegalStateException("Could not determine database type for startup migration.",e);}
  }
}
