package br.eb.ime.agsearch.web;
import br.eb.ime.agprocess.torrent.JobInfo;
import br.eb.ime.agprocess.torrent.JobSessionBean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name = "JobMBean")
@SessionScoped
public class JobMBean implements java.io.Serializable
{
    @EJB
    private JobSessionBean jobSessionBean;
    private JobInfo selectedJob;
    private JobInfo newJob;

    /** Creates a new instance of JobMBean */
    public JobMBean()
    {
    }

    /*
     * Getter method for the newJob property
     */
    public JobInfo getNewJob()
    {
        return newJob;
    }

    /*
     * Setter method for the newJob property
     */
    public void setNewJob(JobInfo newJob)
    {
        this.newJob = newJob;
    }

    /*
     * Getter method for the selectedJob property
     */
    public JobInfo getSelectedJob()
    {
        return selectedJob;
    }

    /*
     * Setter method for the selectedJob property
     */
    public String setSelectedJob(JobInfo selectedJob)
    {
        this.selectedJob = jobSessionBean.getJobInfo(selectedJob);
        return "JobDetails";
    }

    /*
     * Action handler for back to Listing Page
     */
    public String gotoListing()
    {
        return "JobList";
    }

    /*
     * Action handler for New Job button
     */
    public String gotoNew()
    {
        System.out.println("gotoNew() called!!!");
        newJob = new JobInfo();
        return "JobNew";
    }

    /*
     * Action handler for Duplicate button in the Details page
     */
    public String duplicateJob()
    {
        newJob = selectedJob;
        newJob.setJobId("<Job ID>");
        return "JobNew";
    }

    /*
     * Action handler for Update button in the Details page
     */
    public String updateJob()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        try
        {
            selectedJob = jobSessionBean.updateJob(selectedJob);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Success", "Job successfully updated!"));
        }
        catch (Exception ex)
        {
            Logger.getLogger(JobMBean.class.getName()).log(Level.SEVERE, null, ex);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Failed", ex.getCause().getMessage()));
        }
        return null;
    }

    /*
     * Action handler for Delete button in the Details page
     */
    public String deleteJob()
    {
        jobSessionBean.deleteJob(selectedJob);
        return "JobList";
    }

    /*
     * Action handler for Create button in the New page
     */
    public String createJob()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        try
        {
            selectedJob = jobSessionBean.createJob(newJob);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Sucess", "Job successfully created!"));
            return "JobDetails";
        }
        catch (Exception ex)
        {
            Logger.getLogger(JobMBean.class.getName()).log(Level.SEVERE, null, ex);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Failed", ex.getCause().getMessage()));
        }
        return null;
    }
}