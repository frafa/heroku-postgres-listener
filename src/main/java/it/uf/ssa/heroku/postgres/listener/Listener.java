package it.uf.ssa.heroku.postgres.listener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.postgresql.PGConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Listener
{
	private static final Logger logger = LoggerFactory.getLogger(Listener.class);

	@Autowired
	private DataSource dataSource;
	
	@Scheduled(fixedDelay=3000)
	public void listen()
	{
		try(Connection conn =  dataSource.getConnection())
		{
			//Connection conn =  dataSource.getConnection();
			
			// issue a dummy query to contact the backend
			// and receive any pending notifications.
            Statement stmt = conn.createStatement();
            stmt.execute("LISTEN insert_event");
            ResultSet rs = stmt.executeQuery("SELECT 1");
            rs.close();
            stmt.close();
            
            org.postgresql.PGNotification notifications[] = (conn.unwrap(PGConnection.class)).getNotifications();
            
            logger.debug(notifications == null ? "0": String.valueOf(notifications.length));
            
            if (notifications != null) {
                
                for (int i = 0; i < notifications.length; i++) {
                    logger.debug("Got notification: " + notifications[i].getName() + notifications[i].getParameter());
                }
            }
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
