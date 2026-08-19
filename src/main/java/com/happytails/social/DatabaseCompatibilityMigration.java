package com.happytails.social;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Small idempotent compatibility migration for existing Happy Tails databases.
 *
 * The prototype originally relied on Hibernate ddl-auto=update. As the pet
 * identity model grew, an existing production PostgreSQL database could lag
 * behind the Java entity and fail reads/writes before users reached the app.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseCompatibilityMigration implements ApplicationRunner {
  private final JdbcTemplate jdbc;

  public DatabaseCompatibilityMigration(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void run(ApplicationArguments args) {
    add("adoption_date", "date");
    add("gender", "varchar(255)");
    add("personality", "varchar(255)");
    add("favorite_activities", "varchar(255)");
    add("pet_preferences", "varchar(255)");
    add("private_account", "boolean default false");
    add("show_location", "boolean default true");
    add("message_permission", "varchar(255) default 'EVERYONE'");
    add("play_date_permission", "varchar(255) default 'EVERYONE'");

    // Legacy databases created avatar_url as varchar(255). Pet photos are stored
    // as compressed data URLs, so production must allow larger values.
    jdbc.execute("alter table pet_profiles alter column avatar_url type text");

    jdbc.execute("update pet_profiles set private_account=false where private_account is null");
    jdbc.execute("update pet_profiles set show_location=true where show_location is null");
    jdbc.execute("update pet_profiles set message_permission='EVERYONE' where message_permission is null or trim(message_permission)='' ");
    jdbc.execute("update pet_profiles set play_date_permission='EVERYONE' where play_date_permission is null or trim(play_date_permission)='' ");
  }

  private void add(String column, String definition) {
    jdbc.execute("alter table pet_profiles add column if not exists " + column + " " + definition);
  }
}
