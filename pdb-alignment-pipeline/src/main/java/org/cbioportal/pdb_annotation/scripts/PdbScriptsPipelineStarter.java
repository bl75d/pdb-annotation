package org.cbioportal.pdb_annotation.scripts;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Timer;
import org.cbioportal.pdb_annotation.util.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Preliminary script, includes several steps
 * See README for how to run
 *
 * @author Juexin Wang
 *
 */
@SpringBootApplication
@PropertySource("classpath:application.properties")
@EnableScheduling
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class PdbScriptsPipelineStarter {
    public static final String INITIALIZE_DATABASE_COMMAND = "init";
    public static final String WEEKLY_UPDATE_COMMAND = "updateweekly";
    public static final String IMMEDIATE_UPDATE_COMMAND = "update";
    /**
     * main function, run the commands
     *
     * @param args
     */
    public static void main(String[] args) {
        // Check arguments
        if (args.length != 1) {
            System.out.println("Usage:\n"
                    + "Initiate the database"
                    + "java -jar pdb-alignment-pipeline init\n"
                    + "or\n"
                    + "Update the database weekly, user could deploy and change the settings in application.approperties\n"
                    + "java -jar pdb-alignment-pipeline updateweekly\n"
                    + "or\n"
                    + "update for update immediately , users should use CRON or other scheduling mechanisms to run the updates\n"
                    + "java -jar pdb-alignment-pipeline update\n");
            System.exit(0);
        }
        long startTime = System.currentTimeMillis();
        PdbScriptsPipelineRunCommand app = null;
        switch (args[0].toLowerCase()) {
        case INITIALIZE_DATABASE_COMMAND:
            // initialize the database
            app = new PdbScriptsPipelineRunCommand();
            app.runInit();
            break;
        case WEEKLY_UPDATE_COMMAND:
            // user could deploy and change the settings in application.properties
            // the program run continuously, scheduling updates on a weekly basis
            ReadConfig rc = new ReadConfig();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Integer.parseInt(rc.updateDAY_OF_WEEK));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(rc.updateHOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, Integer.parseInt(rc.updateMINUTE));
            calendar.set(Calendar.SECOND, Integer.parseInt(rc.updateSECOND));
            calendar.set(Calendar.MILLISECOND, Integer.parseInt(rc.updateMILLISECOND));
            Timer time = new Timer();
            // PDB is updated at Tuesday, 5 pm PDT during daylight saving time in the US, and 4 pm PST otherwise
            // We choose running the task on Tuesday at Central Time 19:10:00
            time.schedule(new ScheduleUpdateTask(), calendar.getTime(), Integer.parseInt(rc.updateDELAY));
            break;
        case IMMEDIATE_UPDATE_COMMAND:
            // update immediately then exit; users should use CRON or other scheduling mechanisms to run the updates
            app = new PdbScriptsPipelineRunCommand();
            app.runUpdatePDB();
        default:
            System.out.println("The argument should be " + INITIALIZE_DATABASE_COMMAND + ", " +
                    WEEKLY_UPDATE_COMMAND + " or " + IMMEDIATE_UPDATE_COMMAND + "\n");
            break;
        }
        long endTime = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.000");
        System.out.println("[Shell] All Execution time is " + formatter.format((endTime - startTime) / 1000d) + " seconds");
    }
}
