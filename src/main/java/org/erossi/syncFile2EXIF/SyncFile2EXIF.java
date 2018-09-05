package org.erossi.syncFile2EXIF;

import java.io.File;
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
import java.util.TimeZone;
import java.util.jar.JarInputStream;
import java.util.stream.StreamSupport;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import org.apache.commons.io.FilenameUtils;
import com.google.common.base.Strings;

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
        System.out.format("\t\t [command] -> help\n");   
        System.out.format("\t\t [command] -> times java -jar <jarFileName> times <directory> <fileNamePattern>\n");   
        System.out.format("\t\t\t ex.: java -jar <jarFileName> times . *.jpg\n");     
        System.out.format("\t\t [command] -> name java -jar <jarFileName> name <directory> <fileNamePattern>\n");   
        System.out.format("\t\t\t ex.: java -jar <jarFileName> name . *.jpg\n");                        
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
      if (args[0].equalsIgnoreCase("name")) {
        if (args.length < 3 || args[1].isEmpty() || args[2].isEmpty()) {
          System.out.format("\tNo file(s) specified... try \"help\" command for instructions.\n");
        } else {
          File inputDir = new File(args[1]);
          String filePattern = args[2].replace("*", "");
          Arrays.stream(inputDir.listFiles())
            .filter(f -> f.getName().contains(filePattern))
            .forEach(f -> main.syncName(f));
        }
      }        
    }

    public void syncTimes(File inputFile) {
      try {        
        Date exifDateTime = this.getEXIFOriginalDateTime(inputFile);
        if (exifDateTime != null) {          
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

    public void syncName(File inputFile) {
      try {
        Date exifDateTime = this.getEXIFOriginalDateTime(inputFile);
        if (exifDateTime != null) {
          String newNameBase = new SimpleDateFormat("yyyyMMddHHmmss").format(exifDateTime);
          String inputFileExtension = FilenameUtils.getExtension(inputFile.getAbsolutePath());
          if (Strings.isNullOrEmpty(inputFileExtension)) {
            inputFileExtension = "";
          } else {
            inputFileExtension = ".".concat(inputFileExtension);
          }
          Path inputFilePath = Paths.get(inputFile.getAbsolutePath());
          Files.move(inputFilePath, inputFilePath.resolveSibling(newNameBase.concat(inputFileExtension)));
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

    private Date getEXIFOriginalDateTime(File f) throws ImageProcessingException, IOException, ParseException {
      Date exifDateTime = null;
      Metadata metadata = ImageMetadataReader.readMetadata(f);      
      Optional<Tag> o_dateTime = StreamSupport.stream(metadata.getDirectories().spliterator(), false)
        .flatMap(d -> d.getTags().stream())
        .distinct()
        //.forEach(t -> System.out.format("[%s] = |%s|\n", t.getTagName(), t.getDescription()));
        .filter(t -> t.getTagName().equalsIgnoreCase("Date/Time Original"))
        .findFirst();    
      if (o_dateTime.isPresent()) {
        String exifDateTimeString = o_dateTime.get().getDescription();
        DateFormat exifDateTimeStringFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");  
        exifDateTimeStringFormat.setTimeZone(TimeZone.getTimeZone("UTC"));                  
        exifDateTime = exifDateTimeStringFormat.parse(exifDateTimeString);         
      }
      return exifDateTime;
    }
}
