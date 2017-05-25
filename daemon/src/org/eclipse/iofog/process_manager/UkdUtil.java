package org.eclipse.iofog.process_manager;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.eclipse.iofog.utils.logging.LoggingService;
import org.eclipse.iofog.element.Registry;
import org.eclipse.iofog.element.Element;
import org.eclipse.iofog.element.ElementStatus;
import org.eclipse.iofog.utils.Constants;
import org.eclipse.iofog.utils.Constants.ElementState;
import com.sun.management.OperatingSystemMXBean;

// This is one hard-to-avoid import
import com.github.dockerjava.api.model.Container;
/**
 * Provides methods for Ukd commands
 *
 * @author rean
 */
public class UkdUtil {
   private final String MODULE_NAME = "Ukd Util";
   private static UkdUtil instance;

   // Private ctor
   private UkdUtil() {
   }

   public static UkdUtil getInstance() {
      if (instance == null) {
         synchronized(UkdUtil.class) {
            if (instance == null ) {
               instance = new UkdUtil();
            }
         }
      }
      return instance;
   }

   // Stub methods for compile-time compatibility

   /**
    * Check whether Ukd is connected or not
    */
   public boolean isConnected() {
      // Always return false because ukd is running locally
      return false;
   }

   /**
    * connects to ukd daemon
    *
    * @throws Exception
    */
   public void connect() throws Exception {
      try {
         // Do nothing because ukd is running locally (that may change later)
         // so leave the catch block
      } catch (Exception e) {
         StringBuffer msg = new StringBuffer();
         msg.append("connecting to ukd failed: ")
            .append(e.getClass().getName())
            .append(" - ")
            .append(e.getMessage());
         LoggingService.logInfo(MODULE_NAME, msg.toString());
         throw e;
      }
   }

   /**
    * closes ukd daemon connection
    *
    */
   public void close() {
      // Do nothing because ukd is running locally (for now)
   }

   // We *should* be able to re-use the iotracks notion of a Registry.
   // In the worst case we have to subclass it and re-wire things such
   // that "the right thing" comes back from elementManager.getRegistry
   // for users of UkdUtil

   /**
    * logs in to a {@link Registry}
    *
    * @param registry - {@link Registry}
    * @throws Exception
    */
   public void login(Registry registry) throws Exception {
      if (!isConnected()) {
         try {
            connect();
         } catch (Exception e) {
            throw e;
         }
      }
      LoggingService.logInfo(MODULE_NAME, "logging in to registry");

      try {
         // Do nothing because ukd is running locally and we're the only user
      } catch (Exception e ) {
         StringBuffer msg = new StringBuffer();
         msg.append("login failed - " + e.getMessage());
         LoggingService.logWarning(MODULE_NAME, msg.toString());
         throw e;
      }
   }

   /**
    * gets {@link Container} status
    *
    * @param id - id of {@link Container}
    * @return {@link ElementStatus}
    * @throws Exception
    */
   public ElementStatus getContainerStatus(String id) throws Exception {
      StringBuffer cmd = new StringBuffer();
      cmd.append(String.format("ukdctl status --name %s", id));
      ExecutionResponse res = runCommand(cmd.toString());
      ElementStatus result = new ElementStatus();
      result.setStatus(ElementState.STOPPED);
      LoggingService.logInfo(MODULE_NAME, res.getStdout());
      if (res.getStdout().contains("RUNNING")) {
          result.setStatus(ElementState.RUNNING);
      }
      return result;
   }

   /**
    * returns whether the {@link Container} exists or not
    *
    * @param containerId - id of {@link Container}
    * @return
    */
   public boolean hasContainer(String containerId) {
      try {
         getContainerStatus(containerId);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   public String getContainerIpAddress(String id) throws Exception {
      try {
         return "192.168.122.89";
         // throw new Exception("Assuming default uk IP address: 192.168.122.89");
      } catch (Exception e) {
         throw e;
      }
   }

   // We also should be able to reuse the iotracks notion of an Element,
   // which keeps the association between an image name, port mappings etc.

   // Note typo in name (comp*r*are vs. compare) to match
   // DockerUtil::comprarePorts.
   /**
    * compares whether an {@link Element} {@link PortMapping} is
    * same as its corresponding {@link Container} or not
    *
    * @param element - {@link Element}
    * @return boolean
    */
   public boolean comprarePorts(Element element) {
      // Always return true (for now) so we don't need to change the
      // ProcessManager
      return true;
   }

   // The Container class here is provided by the java api model of Docker.
   // We might have to subclass it or introduce a wrapper class that
   // both DockerUtil and UkdUtil can use

   /**
    * returns list of {@link Container} installed on ukd daemon
    *
    * @return list of {@link Container}
    */
   public List<Container> getContainers() {
      if (!isConnected()) {
         try {
            connect();
         } catch (Exception e) {
            // Don't return null, getContainer() never checks for a null list
            return new ArrayList<Container>();
         }
      }
      return new ArrayList<Container>();
   }

   /**
    * returns a {@link Container} if exists
    *
    * @param elementId - name of {@link Container} (id of {@link Element})
    * @return
    */
   public Container getContainer(String elementId) {
      List<Container> containers = getContainers();
      // Assumes containers is never null
      Optional<Container> result = containers.stream()
         .filter(c -> c.getNames()[0].trim().substring(1)
                 .equals(elementId)).findFirst();
      if (result.isPresent())
         return result.get();
      else
         return null;
   }

   /**
    * pulls {@link Image} from {@link Registry}
    *
    * @param imageName - imageName of {@link Element}
    * @throws Exception
    */
   public void pullImage(String imageName) throws Exception {
      // Do nothing (for now)
   }

   /**
    * removes a ukd {@link Image}
    *
    * @param imageName - imageName of {@link Element}
    * @throws Exception
    */
   public void removeImage(String imageName) throws Exception {
      // Do nothing (for now)
   }

   /**
	 * creates {@link Container}
	 *
	 * @param element - {@link Element}
	 * @param host - host ip address
	 * @return id of created {@link Container}
	 * @throws Exception
	 */
   public String createContainer(Element Element, String host)
      throws Exception {
      // Return hardcorded (default) uk instance id
      return "HelloWorldApp";
   }

   /**
    * starts a {@link Container}
    *
    * @param id - id of {@link Container}
    * @throws Exception
    */
   public void startContainer(String id) throws Exception {
      // We may not want to do the mem check
      long totalMemory = ((OperatingSystemMXBean) ManagementFactory
                          .getOperatingSystemMXBean())
         .getTotalPhysicalMemorySize();
      long jvmMemory = Runtime.getRuntime().maxMemory();
      long requiredMemory = (long) Math
         .min(totalMemory * 0.25, 256 * Constants.MiB);

      if (totalMemory - jvmMemory < requiredMemory)
         throw new Exception("Not enough memory to start the container");

      // [Call out] to ukdctl and well known image with pre-decided name
      // ukdctl start --image-location "/tmp/update-test2/c/old.img" --name HelloWorldApp
      // Overwrite is with default app name - HelloWorldApp
      id = "HelloWorldApp";
      String imageLocation = "/osv-images";
      String imageName = "aarch64-loader.img";
      StringBuffer cmd = new StringBuffer();
      cmd.append(String.format("ukdctl start --image-location %s/%s --name %s",
                               imageLocation, imageName, id));
      ExecutionResponse res = runCommand(cmd.toString());
      LoggingService.logInfo(MODULE_NAME, cmd.toString());
      LoggingService.logInfo(MODULE_NAME, res.getStdout());
      // Parse the response - if it's not what we expect then throw an exception
   }

   /**
    * stops a {@link Container}
    *
    * @param id - id of {@link Container}
    * @throws Exception
    */
   public void stopContainer(String id) throws Exception {
      // ukdctl stop --name {id}

      StringBuffer cmd = new StringBuffer();
      cmd.append(String.format("ukdctl stop --name %s", id));
      ExecutionResponse res = runCommand(cmd.toString());
      // Parse the response - if it's not what we expect then throw an exception
   }

   /**
    * Helper routine for running commands
    */
   private ExecutionResponse runCommand(String cmd)
      throws IOException, InterruptedException {
      // Debugging
      System.out.println(String.format("Cmd: %s", cmd));
      Process p = Runtime.getRuntime().exec(cmd);
      // Wait for process to finish
      p.waitFor();
      StringBuffer stdoutBuf = new StringBuffer();
      StringBuffer stderrBuf = new StringBuffer();
      // stdout is connected to the inputstream of the process
      BufferedReader stdout =
         new BufferedReader(new InputStreamReader(p.getInputStream()));

      // stderr is connected to the errorstream of the process
      BufferedReader stderr =
         new BufferedReader(new InputStreamReader(p.getErrorStream()));

      while(stdout.ready()) {
         String line = stdout.readLine();
         if (line != null) {
            stdoutBuf.append(line).append("\n");
         }
      }

      while(stderr.ready()) {
         String line = stderr.readLine();
         if (line != null) {
            stderrBuf.append(line).append("\n");
         }
      }
      // Debugging
      System.out.println(String
                         .format("Process exit code %d, stdout: %s, stderr: %s",
                                 p.exitValue(),
                                 stdoutBuf.toString(),
                                 stderrBuf.toString()));

      return new ExecutionResponse(p.exitValue(),
                                   stdoutBuf.toString(),
                                   stderrBuf.toString());
   }

   /**
    * removes a {@link Container}
    *
    * @param id - id of {@link Container}
    * @throws Exception
    */
   public void removeContainer(String id) throws Exception {
      // Do nothing (for now)
   }

   // Simple test
   public static void main(String[] args) {
      System.out.println("UdkUtil test");

      try {
         UkdUtil ukd = UkdUtil.getInstance();
         ukd.startContainer("foo");
         ukd.stopContainer("foo");
      } catch (Exception e) {
         System.out.println("Exception: " + e.toString());
      }
   }
}
