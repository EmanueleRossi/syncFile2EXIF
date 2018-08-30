package org.erossi.syncFile2EXIF;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.StreamSupport;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class SyncFile2EXIF {

    public static void main(String[] args) {  

      SyncFile2EXIF main = new SyncFile2EXIF();
      String gitHash = new String();
      try (JarInputStream jIS = new JarInputStream(main.getClass().getProtectionDomain().getCodeSource().getLocation().openStream())) {
        gitHash = jIS.getManifest().getMainAttributes().getValue("Git-Hash");
      } catch (Exception e) {
        gitHash = "...under development ;)";                
      } 
      System.out.format("### SyncFile2EXIF ### - Version %s\n", gitHash);

      if (args[0].equalsIgnoreCase("help")) {
        System.out.format("\tUsage: java -jar <jarFileName> [command] [parameters]\n");
        System.out.format("\t\t [command] = help, time\n");   
        System.out.format("\t\t ex.: java -jar <jarFileName> time . *.jpg\b");               
      }
      if (args[0].equalsIgnoreCase("times")) {
        if (args.length < 3 || args[1].isEmpty() || args[2].isEmpty()) {
          System.out.format("\tNo file(s) specified... try \"help\" command for instructions.\n");
        } else {
          File inputDir = new File(args[1]);
          String filePattern = args[2].replace("*", "");
          Arrays.stream(inputDir.listFiles())
            .filter(f -> f.getName().contains(filePattern))
            .forEach(f -> main.syncTimes(f));
        }
      }      
    }

    public void syncTimes(File inputFile) {
      try { 
        Metadata metadata = ImageMetadataReader.readMetadata(inputFile);      
        Optional<Tag> o_dateTime = StreamSupport.stream(metadata.getDirectories().spliterator(), false)
          .flatMap(d -> d.getTags().stream())
          .distinct()
          //.forEach(t -> System.out.format("[%s] = |%s|\n", t.getTagName(), t.getDescription()));
          .filter(t -> t.getTagName().equalsIgnoreCase("Date/Time Original"))
          .findFirst();         

        if (o_dateTime.isPresent()) {
          String exifDateTimeString = o_dateTime.get().getDescription();
          //System.out.println(exifDateTimeString);
          DateFormat exifDateTimeStringFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");  
          exifDateTimeStringFormat.setTimeZone(TimeZone.getTimeZone("UTC"));                  
          Date exifDateTime = exifDateTimeStringFormat.parse(exifDateTimeString); 

          BasicFileAttributeView inputFileView = Files.getFileAttributeView(Paths.get(inputFile.getAbsolutePath()), BasicFileAttributeView.class);
          inputFileView.setTimes(FileTime.fromMillis(exifDateTime.getTime()), FileTime.fromMillis(exifDateTime.getTime()), FileTime.fromMillis(exifDateTime.getTime()));
        } else {
          System.out.format("%s |%s| %s |%s|", "Cannot find \"Date/Time Original\" tag into EXIF metadata of file", inputFile.getAbsolutePath());          
        }
      } catch (IOException ioe) {
        System.err.format("%s %s |%s|", "ERROR! Reading file :(", ioe, inputFile.getAbsolutePath());        
      } catch (ImageProcessingException ipe) {
        System.err.format("%s %s |%s|", "ERROR! Getting Image Metadata :(", ipe, inputFile.getAbsolutePath());       
      } catch (ParseException pe) {
        System.err.format("%s %s |%s|", "ERROR! Getting Date/Time :(", pe, inputFile.getAbsolutePath());       
      }
    }
}


