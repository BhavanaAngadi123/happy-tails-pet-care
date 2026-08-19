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
 * behind the Java entity and fail reads before users reached the application.
 * These ADD COLUMN IF NOT EXISTS statements make the upgrade explicit and safe
 * for both an existing Render database and a fresh local database.
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

    // Normalize legacy rows so primitive boolean fields and privacy rules have
    // deterministic values after the upgrade.
    jdbc.execute("update pet_profiles set private_account=false where private_account is null");
    jdbc.execute("update pet_profiles set show_location=true where show_location is null");
    jdbc.execute("update pet_profiles set message_permission='EVERYONE' where message_permission is null or trim(message_permission)='' ");
    jdbc.execute("update pet_profiles set play_date_permission='EVERYONE' where play_date_permission is null or trim(play_date_permission)='' ");
  }

  private void add(String column, String definition) {
    jdbc.execute("alter table pet_profiles add column if not exists " + column + " " + definition);
  }
}
