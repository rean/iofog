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

import java.io.File;
import java.util.HashMap;

// CLI handling imports
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;


// This is one hard-to-avoid import
import com.github.dockerjava.api.model.Container;
/**
 * Provides methods for Ukd commands
 *
 * @author Rean Griffith (rean@caa.columbia.edu)
 */
public class UkdUtil {
   private final String MODULE_NAME = "Ukd Util";
   private final static String PROP_UKD_IMAGE_LOCATION = "UKD_IMAGE_LOCATION";
   private final static String DEFAULT_UKD_IMAGE_LOCATION =
      "/var/lib/ukd/images";
   private final static String FLAG_HELP_LONG = "help";
   private final static String FLAG_HELP_SHORT = "h";
   private final static String FLAG_APP_ID_LONG = "app-id";
   private final static String FLAG_APP_ID_SHORT = "a";
   private final static String FLAG_IMAGE_PATH_LONG = "image-path";
   private final static String FLAG_IMAGE_PATH_SHORT = "p";
   private final static String FLAG_IMAGE_NAME_LONG = "image-name";
   private final static String FLAG_IMAGE_NAME_SHORT = "i";
   private static UkdUtil instance;

   private String _imageLocation = DEFAULT_UKD_IMAGE_LOCATION;
   private HashMap<String, UkDetails> _appIdMap =
      new HashMap<String, UkDetails>();

   // Private ctor
   private UkdUtil() {
   }

   public static UkdUtil getInstance() {
      if (instance == null) {
         synchronized(UkdUtil.class) {
            if (instance == null ) {
               instance = new UkdUtil();
               // Set any default properties
               instance
                  .setImageLocation(System
                                    .getProperty(PROP_UKD_IMAGE_LOCATION,
                                                 DEFAULT_UKD_IMAGE_LOCATION));
            }
         }
      }
      return instance;
   }

   public synchronized void setImageLocation(String imageLocation)
   {
      // Check whether image location exists and is a directory
      File imageDir = new File(imageLocation);
      if (imageDir.exists() && imageDir.isDirectory()) {
         _imageLocation = imageLocation;
      } else {
         throw new RuntimeException(String.format("Invalid image location: %s either does not exist or is not a directory", imageLocation));
      }
   }
   public String getImageLocation()
   {
      return _imageLocation;
   }

   public synchronized void addAppIdEntry(String appId, UkDetails details) {
      _appIdMap.put(appId, details);
   }

   public synchronized void removeAppIdEntry(String appId) {
      _appIdMap.remove(appId);
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
         UkDetails details = _appIdMap.get(id);
         if (details != null) {
            return details.getIpAddress();
         } else {
            return ""; // Throw an exception instead?
         }
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
      // Return hardcoded (default) uk instance id
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

      // [Call out] to ukdctl
      // ukdctl start --image-location "/tmp/update-test2/c/old.img" --name HelloWorldApp
      //String imageName = "aarch64-loader.img";
      UkDetails details = _appIdMap.get(id);
      String imageName = details.getImageName();
      StringBuffer cmd = new StringBuffer();
      cmd.append(String.format("ukdctl start --image-location %s/%s --name %s",
                               _imageLocation, imageName, id));
      ExecutionResponse res = runCommand(cmd.toString());
      LoggingService.logInfo(MODULE_NAME, cmd.toString());
      LoggingService.logInfo(MODULE_NAME, res.getStdout());
      // Parse the response - if it's not what we expect then throw an exception
      String parts[] = res.getStderr().split(",");

      // Example start messages
      // Successful start:
      // Process exit code 0, stdout: , stderr: 2017/05/28 20:22:24 Application unikernel started: true, IP: 192.168.122.89, Info: Successful start

      // Failed start:
      // Process exit code 0, stdout: , stderr: 2017/05/28 16:48:44 Application unikernel started: false, IP: , Info: HelloWorldApp is already running. Please choose a different name for the application if you wish to start a second instance using the same image.

      // We expect the response messages to have at least 3 parts
      // Part 1: start status, e.g., 2017/05/28 16:48:44 Application unikernel started: false
      // Part 2: IP address (if started), e.g.,  IP: 192.168.122.89
      // Part 3: Info section with more details, e.g., Info: Successful start

      if (parts.length < 3) {
         throw new Exception(String
                             .format("Unexpected start-command response: %s",
                                     res.getStderr()));
      }

      // Does the first part of the response end in "true" or "false"
      boolean unikernelStarted = false;
      boolean unikernelAlreadyRunning = false;
      String unikernelIP = "";
      if (parts[0].endsWith("true")) {
         unikernelStarted = true;
      }

      if (!unikernelStarted) {
         // Check whether the info message indicates that the app is already
         // running.
         if (parts[2].contains("already running")) {
            unikernelAlreadyRunning = true;
         } else {
            throw new Exception(String
                                .format("Unikernel for app id: %s, not started",
                                        id));
         }
      }

      // Save the IP address of the container, we will need it later
      if (!unikernelAlreadyRunning) {
         unikernelIP = parts[1].trim().split(" ")[1].trim();
         System.out.println(String.format("Unkernel IP address: %s",
                                          unikernelIP));
         details.setIpAddress(unikernelIP);
         _appIdMap.put(id, details);
      }
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

      // Example stop messages
      // Successful stop:
      // Process exit code 0, stdout: , stderr: 2017/05/28 21:33:16 Application unikernel stopped: true, Info: Successfully stopped Application (HelloWorldApp)

      // Failed stop:
      // Process exit code 0, stdout: , stderr: 2017/05/28 21:32:13 Application unikernel stopped: true, Info: App not found. Nothing to do.

      // We expect the response to have at least 2 parts
      // Part 1: stop status, e.g., 2017/05/28 21:33:16 Application unikernel stopped: true
      // Part 2: Info section, e.g.,  Info: Successfully stopped Application (HelloWorldApp)

      String parts[] = res.getStderr().split(",");
      if (parts.length < 2) {
         throw new Exception(String
                             .format("Unexpected stop-command response: %s",
                                     res.getStderr()));
      }
      if (parts[1].contains("Successfully stopped")) {
         removeAppIdEntry(id);
      }
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
2    * removes a {@link Container}
    *
    * @param id - id of {@link Container}
    * @throws Exception
    */
   public void removeContainer(String id) throws Exception {
      // Do nothing (for now)
   }

   public static Options getOptions()
   {
      Options options = new Options();
      options.addOption(FLAG_APP_ID_SHORT, FLAG_APP_ID_LONG,
                        true, "Application Id");
      options.addOption(FLAG_IMAGE_NAME_SHORT, FLAG_IMAGE_NAME_LONG,
                        true, "Name of the image to load");
      options.addOption(FLAG_IMAGE_PATH_SHORT, FLAG_IMAGE_PATH_LONG, true,
                        "Fully qualified path to the directory of images");
      options.addOption(FLAG_HELP_SHORT, FLAG_HELP_LONG, false, "Show usage");
      return options;
   }

   public static void printUsage(Options options)
   {
      PrintWriter writer = new PrintWriter(System.out);
      HelpFormatter usageFormatter = new HelpFormatter();
      String header = "";
      String footer = "";
      int leftPad = 2;
      int descPad = 4;
      usageFormatter.printHelp(writer, 80, "ukdutil", header,
                               options, leftPad, descPad, footer, true);
      writer.flush();
   }

   // Simple test
   public static void main(String[] args) {
      System.out.println("Launching UdkUtil...");

      Options options = getOptions();
      // Parse the command line
      CommandLineParser parser = new PosixParser();
      CommandLine commandLine = null;
      // Parameter values
      String appId = "";
      String imagePath = "";
      String imageName = "";

      if (args.length == 0) {
         printUsage(options);
         System.exit(-1);
      }

      // Parse the command line
      try {
         commandLine = parser.parse(options, args);
      } catch (ParseException pe) {
         System
            .err.println(String.format("Error parsing command line arguments. Reason: %s", pe.getMessage()));
         printUsage(options);
         System.exit(-1);
      }

      // What did we get?
      if (commandLine.hasOption(FLAG_HELP_SHORT) ||
          commandLine.hasOption(FLAG_HELP_LONG)) {
         printUsage(options);
         System.exit(0);
      }

      if (commandLine.hasOption(FLAG_APP_ID_SHORT)) {
         appId = commandLine.getOptionValue(FLAG_APP_ID_SHORT);
      }
      if (commandLine.hasOption(FLAG_APP_ID_LONG)) {
         appId = commandLine.getOptionValue(FLAG_APP_ID_LONG);
      }

      if (commandLine.hasOption(FLAG_IMAGE_PATH_SHORT)) {
         imagePath = commandLine.getOptionValue(FLAG_IMAGE_PATH_SHORT);
      }
      if (commandLine.hasOption(FLAG_IMAGE_PATH_LONG)) {
         imagePath = commandLine.getOptionValue(FLAG_IMAGE_PATH_LONG);
      }

      if (commandLine.hasOption(FLAG_IMAGE_NAME_SHORT)) {
         imageName = commandLine.getOptionValue(FLAG_IMAGE_NAME_SHORT);
      }
      if (commandLine.hasOption(FLAG_IMAGE_NAME_LONG)) {
         imageName = commandLine.getOptionValue(FLAG_IMAGE_NAME_LONG);
      }

      // If we're here we expect the appname, image name and image location
      // to be specified
      if (appId.length() == 0) {
         System.out.println("Error: Non-empty application id required");
         printUsage(options);
         System.exit(-1);
      }

      if (imageName.length() == 0) {
         System.out.println("Error: Non-empty image name required");
         printUsage(options);
         System.exit(-1);
      }

      if (imagePath.length() == 0) {
         // Use the default image path
         imagePath = DEFAULT_UKD_IMAGE_LOCATION;
         System.out
            .println(String.format("Warning: Using default image path: %s",
                                   imagePath));
      }

      try {
         UkdUtil ukd = UkdUtil.getInstance();
         // Set key properties, e.g., the image path and image name
         ukd.setImageLocation(imagePath);
         UkDetails details = new UkDetails();
         details.setAppId(appId);
         details.setImageName(imageName);

         ukd.addAppIdEntry(appId, details);
         ukd.startContainer(appId);
         ukd.stopContainer(appId);
      } catch (Exception e) {
         System.out.println("Exception: " + e.toString());
         e.printStackTrace();
      }
   }
}
