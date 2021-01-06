package backend;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

public class Datastore {
	
	private Integer TimeSpendID;
	private String ActivityName;
    private String TaskName;
	private String Description;
	private LocalDate Date;
	private LocalTime StartTime;
	private LocalTime EndTime;
	private Duration Duration;
	
	public Datastore (){
		//Do nothing
	}
	
	public Datastore(Integer TimeSpendID, String ActivityName, String TaskName, String Description, LocalDate Date, LocalTime StartTime, LocalTime EndTime){
		this.TimeSpendID  = TimeSpendID;
		this.ActivityName = ActivityName;
		this.TaskName     = TaskName;
		this.Description  = Description;
		this.Date         = Date;
		this.StartTime    = StartTime;
		this.EndTime      = EndTime;
	}
	
	public void setTimeSpendID(Integer TimeSpendID){
		this.TimeSpendID = TimeSpendID;
	}
	
	public void setActivityName(String ActivityName){
		this.ActivityName = ActivityName;
	}
	
	public void setTaskName(String TaskName){
		this.TaskName = TaskName; 
	}
	
	public void setDescription(String Description){
		this.Description = Description;
	}
	
	public void setDate(LocalDate Date){
		this.Date = Date;
	}
	
	public void setStartTime(LocalTime StartTime){
		this.StartTime = StartTime;
	}
	
	public void setEndTime(LocalTime EndTime){
		this.EndTime = EndTime;
	}
	
	public void setDuration(Duration Duration){
		this.Duration = Duration;
	}
	
	public Integer getTimeSpend(){
		return TimeSpendID;
	}
	
	public String getActivityName(){
		return ActivityName;
	}
	
	public String getTaskName(){
		return TaskName;
	}
	
	public String getDescription(){
		return Description;
	}
	
	public LocalDate getDate(){
		return Date;
	}
	
	public LocalTime getStartTime() {
		return StartTime;
	}
	
	public LocalTime getEndTime() {
		return EndTime;
	}
	
	public Duration getDuration() {
		return Duration;
	}
}