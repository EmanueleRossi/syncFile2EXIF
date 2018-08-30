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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class  SyncFile2EXIF {

    public static void main(String[] args) {   
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
          DateFormat exifDateTimeStringFormat = new SimpleDateFormat("yyyy:mm:dd hh:mm:ss");                    
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


