package com.example.springbatchdemo.config;

import com.example.springbatchdemo.models.Designation;
import com.example.springbatchdemo.repositories.EmployeeRepository;
import com.example.springbatchdemo.models.Employee;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    
    @Bean
    public FlatFileItemReader<Employee> csvReader(@Value("${inputFile}") String inputFile) {
        return new FlatFileItemReaderBuilder<Employee>()
                .name("csv-reader")
                .resource(new ClassPathResource(inputFile))
                .delimited()
                .names("id", "name", "designation")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{setTargetType(Employee.class);}})
                .build();
    }
    
    @Bean
    public RepositoryItemReader<Employee> repositoryReader(EmployeeRepository employeeRepository) {
        return new RepositoryItemReaderBuilder<Employee>()
                .repository(employeeRepository)
                .methodName("findAll")
                .sorts(Map.of("id", Sort.Direction.ASC))
                .name("repository-reader")
                .build();
    }
    
    @Component
    public static class NameProcessor implements ItemProcessor<Employee, Employee> {
        
        @Override
        public Employee process(Employee employee) {
            employee.setName(employee.getName().toUpperCase());
            employee.setNameUpdated(new Date());
            return employee;
        }
    }
    
    @Component
    public static class DesignationProcessor implements ItemProcessor<Employee, Employee> {
        
        @Override
        public Employee process(Employee employee) {
            employee.setDesignation(Designation.getByCode(employee.getDesignation()).getTitle());
            employee.setDesignationUpdated(new Date());
            return employee;
        }
    }

    @Component
    public static class EmployeeWriter implements ItemWriter<Employee> {

        @Autowired
        private EmployeeRepository employeeRepository;

        @Value("${sleepTime}")
        private Integer SLEEP_TIME;

        @Override
        public void write(List<? extends Employee> employees) throws InterruptedException {
            employeeRepository.saveAll(employees);
            Thread.sleep(SLEEP_TIME);
            System.out.println("Saved employees: " + employees);
        }
    }

    @Bean
    public Step nameStep(StepBuilderFactory stepBuilderFactory, ItemReader<Employee> csvReader, NameProcessor processor, EmployeeWriter writer) {
        return stepBuilderFactory.get("name-step")
                .<Employee, Employee>chunk(100)
                .reader(csvReader)
                .processor(processor)
                .writer(writer)
                .allowStartIfComplete(false)
                .build();
    }
    
    @Bean
    public Step designationStep(StepBuilderFactory stepBuilderFactory, ItemReader<Employee> repositoryReader, DesignationProcessor processor, EmployeeWriter writer) {
        return stepBuilderFactory.get("designation-step")
                .<Employee, Employee>chunk(100)
                .reader(repositoryReader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    
    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, Step nameStep, Step designationStep) {
        return jobBuilderFactory.get("employee-loader-job")
                .incrementer(new RunIdIncrementer())
                .start(nameStep)
                .next(designationStep)
                .preventRestart()
                .build();
    }
}
