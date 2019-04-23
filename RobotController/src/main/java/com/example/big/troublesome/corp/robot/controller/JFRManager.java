package com.example.big.troublesome.corp.robot.controller;

import java.io.File;
import java.io.IOException;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.VirtualMachine;

import jdk.management.jfr.FlightRecorderMXBean;

public class JFRManager {

    private long pid;
    private FlightRecorderMXBean jfr;
    private long id;
    
    public void setPid(long pid) {
        this.pid = pid;
    }
    
    public void connect() {
        try {
            System.err.println(pid);

            JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9091/jmxrmi");
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
            MBeanServerConnection mBeanServer = connector.getMBeanServerConnection();

            ObjectName jfrBeanName = new ObjectName("jdk.management.jfr:type=FlightRecorder");
            jfr = JMX.newMXBeanProxy(mBeanServer, jfrBeanName, FlightRecorderMXBean.class);
            
        } catch (Exception e) {
            System.err.println("Could not initialize JFR");
            e.printStackTrace();
        }
    }
    
    public void startRercording() {
        if (jfr == null) {
            return;
        }
        id = jfr.newRecording();
        jfr.startRecording(id);
    }

    public File stopRecording() {
        if (jfr == null) {
            return null;
        }
        
        jfr.stopRecording(id);        
        try {
            File recording = File.createTempFile("robot-maker-xpress-2k-" + id,".jfr");
            System.err.println(recording);
            jfr.copyTo(id, recording.getCanonicalPath());
            jfr.closeRecording(id);
            
            return recording;
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }

}
