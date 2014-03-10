/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.eb.ime.agprocess.torrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.DuplicateKeyException;
import javax.ejb.LocalBean;
import javax.ejb.ScheduleExpression;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author VelasteguiFraga
 */
@Stateless
@LocalBean
public class JobSessionBean
{
    @Resource
    TimerService timerService;	//Resource Injection
    static Logger logger = Logger.getLogger("JobSessionBean");

    /*
     * Callback method for the timers. Calls the corresponding Batch Job Session Bean based on the JobInfo
     * bounded to the timer
     */
    @Timeout
    public void timeout(Timer timer)
    {
        System.out.println("###Timer <" + timer.getInfo() + "> timeout at " + new Date());
        try
        {
            JobInfo jobInfo = (JobInfo) timer.getInfo();
            BatchJobInterface batchJob = (BatchJobInterface) InitialContext.doLookup(
                    jobInfo.getJobClassName());
            batchJob.executeJob(timer); //Asynchronous method
        }
        catch (NamingException ex)
        {
            logger.log(Level.SEVERE, null, ex);
        }
        catch (Exception ex1)
        {
            logger.severe("Exception caught: " + ex1);
        }
    }

    /*
     * Returns the Timer object based on the given JobInfo
     */
    private Timer getTimer(JobInfo jobInfo)
    {
        Collection<Timer> timers = timerService.getTimers();
        for (Timer t : timers)
        {
            if (jobInfo.equals((JobInfo) t.getInfo()))
            {
                return t;
            }
        }
        return null;
    }

    /*
     * Creates a timer based on the information in the JobInfo
     */
    public JobInfo createJob(JobInfo jobInfo)
            throws Exception
    {
        //Check for duplicates
        if (getTimer(jobInfo) != null)
        {
            throw new DuplicateKeyException("Job with the ID already exist!");
        }

        TimerConfig timerAConf = new TimerConfig(jobInfo, true);

        ScheduleExpression schedExp = new ScheduleExpression();
        schedExp.start(jobInfo.getStartDate());
        schedExp.end(jobInfo.getEndDate());
        schedExp.second(jobInfo.getSecond());
        schedExp.minute(jobInfo.getMinute());
        schedExp.hour(jobInfo.getHour());
        schedExp.dayOfMonth(jobInfo.getDayOfMonth());
        schedExp.month(jobInfo.getMonth());
        schedExp.year(jobInfo.getYear());
        schedExp.dayOfWeek(jobInfo.getDayOfWeek());
        logger.info("### Scheduler expr: " + schedExp.toString());

        Timer newTimer = timerService.createCalendarTimer(schedExp, timerAConf);
        logger.info("New timer created: " + newTimer.getInfo());
        jobInfo.setNextTimeout(newTimer.getNextTimeout());

        return jobInfo;
    }

    /*
     * Returns a list of JobInfo for the active timers
     */
    public List<JobInfo> getJobList()
    {
        logger.info("getJobList() called!!!");
        ArrayList<JobInfo> jobList = new ArrayList<JobInfo>();
        Collection<Timer> timers = timerService.getTimers();
        for (Timer t : timers)
        {
            JobInfo jobInfo = (JobInfo) t.getInfo();
            jobInfo.setNextTimeout(t.getNextTimeout());
            jobList.add(jobInfo);
        }
        return jobList;
    }

    /*
     * Returns the updated JobInfo from the timer
     */
    public JobInfo getJobInfo(JobInfo jobInfo)
    {
        Timer t = getTimer(jobInfo);
        if (t != null)
        {
            JobInfo j = (JobInfo) t.getInfo();
            j.setNextTimeout(t.getNextTimeout());
            return j;
        }
        return null;
    }

    /*
     * Updates a timer with the given JobInfo
     */
    public JobInfo updateJob(JobInfo jobInfo)
            throws Exception
    {
        Timer t = getTimer(jobInfo);
        if (t != null)
        {
            logger.info("Removing timer: " + t.getInfo());
            t.cancel();
            return createJob(jobInfo);
        }
        return null;
    }

    /*
     * Remove a timer with the given JobInfo
     */
    public void deleteJob(JobInfo jobInfo)
    {
        Timer t = getTimer(jobInfo);
        if (t != null)
        {
            t.cancel();
        }
    }
}