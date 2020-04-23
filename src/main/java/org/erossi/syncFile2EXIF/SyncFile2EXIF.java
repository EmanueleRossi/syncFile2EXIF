package org.erossi.syncFile2EXIF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.jar.JarInputStream;
import java.util.stream.StreamSupport;

import com.adobe.xmp.XMPDateTime;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import org.apache.commons.io.FilenameUtils;

public class SyncFile2EXIF {

    private final static String TIME_ZONE = "GMT";
    public static void main(String[] args) {  

      SyncFile2EXIF main = new SyncFile2EXIF();
      String gitHash = new String();
      try (JarInputStream jIS = new JarInputStream(main.getClass().getProtectionDomain().getCodeSource().getLocation().openStream())) {
        gitHash = jIS.getManifest().getMainAttributes().getValue("Git-Hash");
      } catch (Exception e) {
        gitHash = "...under development ;)";                
      } 
      System.out.format("### SyncFile2EXIF ### - Version %s\n", gitHash);

      if (args[0].contains("help")) {
        System.out.format("\tUsage: java -jar <jarFileName> [command] [parameters]\n");
        System.out.format("\t\t [command] -> help\n");   
        System.out.format("\t\t [command] -> times java -jar <jarFileName> times <directory> <fileNamePattern>\n");   
        System.out.format("\t\t\t ex.: java -jar <jarFileName> times . .jpg\n");     
        System.out.format("\t\t [command] -> name java -jar <jarFileName> name <directory> <fileNamePattern>\n");   
        System.out.format("\t\t\t ex.: java -jar <jarFileName> name . .jpg\n");                       
        System.out.format("\t\t [command] -> setTimes java -jar <jarFileName> setTimes <directory> <fileNamePattern> <yyyyMMddHHmmss>\n");   
        System.out.format("\t\t\t ex.: java -jar <jarFileName> setTimes . .jpg 20181225120000\n");          
      }
      if (args[0].contains("times")) {
        if (args.length < 3 || args[1].isEmpty() || args[2].isEmpty()) {
          System.out.format("\tNo file(s) specified... try \"help\" command for instructions.\n");
        } else {
          File inputDir = new File(args[1]);
          String filePattern = args[2];
          Arrays.stream(inputDir.listFiles())
            .filter(f -> f.getName().contains(filePattern))
            .forEach(f -> main.setTimes(f, main.getEXIFOriginalDateTime(f)));
        }
      }    
      if (args[0].contains("timesXMP")) {
        if (args.length < 3 || args[1].isEmpty() || args[2].isEmpty()) {
          System.out.format("\tNo file(s) specified... try \"help\" command for instructions.\n");
        } else {
          File inputDir = new File(args[1]);
          String filePattern = args[2];
          Arrays.stream(inputDir.listFiles())
            .filter(f -> f.getName().contains(filePattern))
            .forEach(f -> main.setTimes(f, main.getDateCreatedFromXMP(f)));
        }
      }       
      if (args[0].contains("name")) {
        if (args.length < 3 || args[1].isEmpty() || args[2].isEmpty()) { 
          System.out.format("\tNo file(s) specified... try \"help\" command for instructions.\n");
        } else {      
          File inputDir = new File(args[1]);
          String filePattern = args[2];
          Arrays.stream(inputDir.listFiles())
            .filter(f -> f.getName().contains(filePattern))
            .forEach(f -> main.setName(f, main.getEXIFOriginalDateTime(f)));          
        }
      } 
      if (args[0].contains("setTimes")) {
        if (args.length < 3 || args[1].isEmpty() || args[2].isEmpty() || args[3].isEmpty()) {
          System.out.format("\tNo file(s) specified... try \"help\" command for instructions.\n");
        } else {
          File inputDir = new File(args[1]);
          String filePattern = args[2];
          try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone(SyncFile2EXIF.TIME_ZONE));          
            Date time = sdf.parse(args[3]);
            Arrays.stream(inputDir.listFiles())
              .filter(f -> f.getName().contains(filePattern))
              .forEach(f -> main.setTimes(f, time));
          } catch (ParseException pe) {
            System.err.format("%s |%s|", "ERROR! Wrong input date string!  :(", args[3]);       
          }
        }
      }              
    }

    public void setTimes(File inputFile, Date date) {
      try {        
        if (date != null) {          
          BasicFileAttributeView inputFileView = Files.getFileAttributeView(Paths.get(inputFile.getAbsolutePath()), BasicFileAttributeView.class);
          inputFileView.setTimes(FileTime.fromMillis(date.getTime()), FileTime.fromMillis(date.getTime()), FileTime.fromMillis(date.getTime()));
        } else {
          System.out.format("%s |%s|", "Can't update file times with {null} date!");          
        }
      } catch (IOException ioe) {
        System.err.format("%s %s |%s|", "ERROR! Reading file :(", ioe, inputFile.getAbsolutePath());        
      } 
    }

    public void setName(File inputFile, Date date) {
      try {
        if (date != null) {
          String newNameBase = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
          String inputFileExtension = FilenameUtils.getExtension(inputFile.getAbsolutePath());
          if (inputFileExtension == null | inputFileExtension == "") {
            inputFileExtension = "";
          } else {
            inputFileExtension = ".".concat(inputFileExtension);
          }
          Path inputFilePath = Paths.get(inputFile.getAbsolutePath());               
          Path outputFilePath = inputFilePath.resolveSibling(newNameBase.concat(inputFileExtension));
          if (outputFilePath.toFile().exists()) {            
            newNameBase = newNameBase.concat(String.format("-%04d", new Random().nextInt(1000)));
            outputFilePath = inputFilePath.resolveSibling(newNameBase.concat(inputFileExtension));
          }
          Files.move(inputFilePath, outputFilePath);
        } else {
          System.out.format("%s |%s|", "Cannot find \"Date/Time Original\" tag into EXIF metadata of file", inputFile.getAbsolutePath());          
        }
      } catch (IOException ioe) {
        System.err.format("%s %s |%s|", "ERROR! Reading file :(", ioe, inputFile.getAbsolutePath());        
      } 
    }

    public Date getEXIFOriginalDateTime(File f) {
      Date exifDateTime = null;
      try {
        Metadata metadata = ImageMetadataReader.readMetadata(f);      
        Optional<Tag> o_dateTime = StreamSupport.stream(metadata.getDirectories().spliterator(), false)
          .flatMap(d -> d.getTags().stream())
          .distinct()
          //.forEach(t -> System.out.format("[%s] = |%s|\n", t.getTagName(), t.getDescription()))
          .filter(t -> t.getTagName().equalsIgnoreCase("Date/Time Original"))
          .findFirst();    
        if (o_dateTime.isPresent()) {
          String exifDateTimeString = o_dateTime.get().getDescription();
          DateFormat exifDateTimeStringFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");                    
          exifDateTimeStringFormat.setTimeZone(TimeZone.getTimeZone(SyncFile2EXIF.TIME_ZONE));            
          exifDateTime = exifDateTimeStringFormat.parse(exifDateTimeString);         
        }
      } catch (ImageProcessingException ipe) {
        System.err.format("%s %s |%s|", "ERROR! Getting Image Metadata :(", ipe, f.getAbsolutePath());       
      } catch (ParseException pe) {
        System.err.format("%s %s |%s|", "ERROR! Getting Date/Time :(", pe, f.getAbsolutePath());       
      } catch (IOException ioe) {
        System.err.format("%s %s |%s|", "ERROR! Reading file :(", ioe, f.getAbsolutePath());        
      } 
      return exifDateTime;
    }

    public Date getDateCreatedFromXMP(File f) {
      Date exifDateCreated = null;
      try {
        File xmpFile = new File(f.getPath().replace(FilenameUtils.getExtension(f.getAbsolutePath()), "xmp"));
        XMPMeta xmpMeta = XMPMetaFactory.parse(new FileInputStream(xmpFile));
        XMPDateTime xmpDateTime = xmpMeta.getPropertyDate("http://ns.adobe.com/photoshop/1.0/", "DateCreated");
        exifDateCreated = xmpDateTime.getCalendar().getTime();
      } catch (XMPException xe) {
        System.err.format("%s %s |%s|", "ERROR! Reading XMP file :(", xe, f.getAbsolutePath());        
      } catch (FileNotFoundException fnf) {
        System.err.format("%s %s |%s|", "ERROR! Reading file :(", fnf, f.getAbsolutePath());        
      } 
      return exifDateCreated;
    }
}
