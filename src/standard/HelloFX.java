package standard;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage primaryStage) {
    	
    	Map<String, List<String>> Activity = DataBase.initiateDate();
    	
    	primaryStage.setTitle("BearingPoint Caribbean");
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 650, 100);
        primaryStage.setScene(scene);

        ObservableList<String> Activities =
        		
        	    FXCollections.observableArrayList(
        	        Activity.keySet()
        	    );
        	    
        	final ComboBox<String> cmbBxActivities = new ComboBox<>(Activities);
			cmbBxActivities.setPromptText("Activity");
			cmbBxActivities.setEditable(true);
			cmbBxActivities.setDisable(false);
        grid.add(cmbBxActivities, 1, 1);
        
    	    final ComboBox<String> cmbBxTasks = new ComboBox<>();
			cmbBxTasks.setPromptText("Tasks");
			cmbBxTasks.setEditable(false);
			cmbBxTasks.setDisable(true);
        grid.add(cmbBxTasks, 2, 1);
        
        TextField userTextField = new TextField();
        grid.add(userTextField, 3, 1);

        
        // Buttons
        
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
       
        Button btnStart = new Button("Start");
        hbBtn.getChildren().add(btnStart);
        btnStart.setDisable(true);
        grid.add(hbBtn, 3, 2);
        
        Button btnStop = new Button("Stop");
        hbBtn.getChildren().add(btnStop);
        btnStop.setDisable(true);
        
        Button btnDB = new Button("DB");
        hbBtn.getChildren().add(btnDB);

        
        // Set values with active Activity Task
        try {
			String[] openActivity = DataBase.checkOpenActivityTask();
			if (openActivity[0] != null) {
				cmbBxActivities.setValue(openActivity[0]);
				cmbBxActivities.setDisable(true);
		
				cmbBxTasks.setValue(openActivity[1]);
				userTextField.setDisable(true);
			    userTextField.setText(openActivity[2]);
			    cmbBxTasks.setDisable(true);

                btnStop.setDisable(false);
                btnStart.setDisable(true);
			}
		} catch (ClassNotFoundException | SQLException e2) {
			e2.printStackTrace();
		}

        // Action handlers
        
        cmbBxActivities.setOnAction(new EventHandler<ActionEvent>() {
        	
        	@Override
        	public void handle(ActionEvent e ) {
        		btnStart.setDisable(false);
				ObservableList<String> Tasks = null;
				if (Activity.containsKey(cmbBxActivities.getValue())) {
				    Tasks = 
                	    FXCollections.observableArrayList(
                	        Activity.get(cmbBxActivities.getValue())
                	    );
                }
				cmbBxTasks.setItems(Tasks);
				cmbBxTasks.setEditable(true);
				cmbBxTasks.setDisable(false);
				
        	}
        });
        
        btnStart.setOnAction(new EventHandler<ActionEvent>() {
       	 
            @Override
            public void handle(ActionEvent e){
                if (cmbBxActivities.getValue() != null  && userTextField.getLength() != 0) {
                    btnStop.setDisable(false);
                    btnStart.setDisable(true);
                    cmbBxActivities.setDisable(true);
                    cmbBxTasks.setDisable(true);
                    userTextField.setDisable(true);
					
					String sql[] = new String[3];
					
					if (!Activity.containsKey(cmbBxActivities.getValue())) {
						sql[0] = "INSERT INTO Activity (ActivityName, isActive) VALUES ('" + cmbBxActivities.getValue() + "', true)";
						if (cmbBxTasks.getValue() != null) {
							sql[1] = "INSERT INTO Task (TaskID, TaskName, ActivityName, isActive) VALUES (0,'" + cmbBxTasks.getValue() + "', '" + cmbBxActivities.getValue() + "', true)";
						}
					}
					
					if (Activity.containsKey(cmbBxActivities.getValue())) {
                        if(!Activity.get(cmbBxActivities.getValue()).contains(cmbBxTasks.getValue())) {
					       sql[1] = "INSERT INTO Task (TaskID, TaskName, ActivityName, isActive) VALUES (0,'" + cmbBxTasks.getValue() + "', '" + cmbBxActivities.getValue() + "', true)";
                           System.out.println(sql[1]);
						}						
					}

                    sql[2] = "INSERT INTO TimeSpend (Description, ActivityName, TaskID, Date, StartTime, EndTime, isActive)\r\n" +
                             "VALUES ('" + userTextField.getText() + "', '" +  cmbBxActivities.getValue() + "', " +
                             "(SELECT TaskID FROM Task WHERE TaskName = '" + cmbBxTasks.getValue() + "'), '" + LocalDate.now() + "', '" +
                             LocalTime.now() + "', '" + LocalTime.now() + "', true)";
					
					for(int i = 0; i < sql.length; i++ ){
						if (sql[i] != null) {	
                            try {
					            DataBase.writeDB(sql[i]);
				            } catch (ClassNotFoundException e1) {
					            e1.printStackTrace();
				            } catch (SQLException e1) {
					           e1.printStackTrace();
				            }
						}
					}
					
                }
            }
        });
                
        btnStop.setOnAction(new EventHandler<ActionEvent>() {
       	 
            @Override
            public void handle(ActionEvent e) {
                btnStop.setDisable(true);
                btnStart.setDisable(false);
                cmbBxActivities.setDisable(false);
                cmbBxTasks.setDisable(false);
                userTextField.setDisable(false);

                try {
					DataBase.closeActivityTask(cmbBxActivities.getValue(), cmbBxTasks.getValue());
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
            }
        });
        
        btnDB.setOnAction(new EventHandler<ActionEvent>() { 
        	
        	@Override
        	public void handle(ActionEvent e) {
        		try {
					DataBase.openDBManager();
				} catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
				}
        	}
        	
        });
      
        primaryStage.show();
    }
}
