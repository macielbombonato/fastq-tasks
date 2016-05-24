package bombonato.fastq.batch.config;

import bombonato.fastq.batch.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.File;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackages = {
        "bombonato"
})
@EnableTransactionManagement
public class DatabaseConfig {

    @Autowired
    Environment env;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[] { "bombonato" });

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalJpaProperties());

        return em;
    }

    Properties additionalJpaProperties() {
        Properties properties = new Properties();

        properties.setProperty("hibernate.hbm2ddl.auto", "create");

        // MySQL Database
//        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");

        // HSQL Database
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

        properties.setProperty("hibernate.show_sql", "false");

        return properties;
    }

    @Bean
    public DataSource dataSource(){
        // Local MySQL Database
//        String driver = "com.mysql.jdbc.Driver";
//        String url = "jdbc:mysql://localhost:3306/bombonato_fastq?createDatabaseIfNotExist=true&amp;useUnicode=true&amp;characterEncoding=utf-8";
//        String username = "root";
//        String password = "root";

        // HSQL Database
        String driver = "org.hsqldb.jdbc.JDBCDriver";
        String url = "jdbc:hsqldb:file:" + Application.datasourceDir + File.separator + "fastqDB";
        String username = "SA";
        String password = "";

        return DataSourceBuilder.create()
                .url(url)
                .driverClassName(driver)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }
}
