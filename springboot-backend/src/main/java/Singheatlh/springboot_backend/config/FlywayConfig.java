package Singheatlh.springboot_backend.config;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

@Configuration
@Order(1) // Ensure this runs early
public class FlywayConfig {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String[] locations;

    /**
     * Force Flyway to run migrations immediately after bean creation.
     * This runs BEFORE Hibernate tries to validate schema.
     */
    @PostConstruct
    public void runFlywayMigrations() {
        System.out.println("🔄 Starting Flyway migrations...");
        
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations(locations)
                    .baselineOnMigrate(true)
                    .schemas("public")
                    .load();
            
            // Repair if needed, then migrate
            flyway.repair();
            int migrationsApplied = flyway.migrate().migrationsExecuted;
            
            System.out.println("✅ Flyway: Successfully applied " + migrationsApplied + " migration(s)");
        } catch (Exception e) {
            System.err.println("❌ Flyway migration failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Flyway migration failed", e);
        }
    }
}
