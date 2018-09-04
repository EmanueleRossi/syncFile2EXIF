import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

import org.apache.commons.io.FileUtils;
import org.erossi.syncFile2EXIF.SyncFile2EXIF;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class SyncFile2EXIFTest {

  private static SyncFile2EXIF tmc;

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();  

  @BeforeClass
  public static void initTest() {
    FileUtils.deleteQuietly(new File(".\\build\\resources\\test\\NonExistentFile.jpg"));
    FileUtils.deleteQuietly(new File(".\\build\\resources\\test\\test-01.jpg"));
    FileUtils.deleteQuietly(new File(".\\build\\resources\\test\\test-02.jpg"));
    FileUtils.deleteQuietly(new File(".\\build\\resources\\test\\test-03.jpg"));    
    tmc = new SyncFile2EXIF();
  }

  @Test
  public void testNonExistingFile() {    
    ExpectedException ioe = ExpectedException.none();

    ioe.expect(IOException.class);
    tmc.syncTimes(new File(".\\build\\resources\\test\\NonExistentFile.jpg"));
  }

  @Test
  public void testNonImageFile() {    
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(outContent));

    tmc.syncTimes(new File(".\\build\\resources\\test\\sample-02.txt"));
    assertNotNull(outContent);
  }  

  @Test
  public void testSamples() throws Exception {
    File copy01 = testFolder.newFile("test-01.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-01.jpg"), copy01);
    tmc.syncTimes(copy01);
    BasicFileAttributes attr01 = Files.readAttributes(Paths.get(copy01.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr01.lastModifiedTime().toInstant()); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr01.lastAccessTime().toInstant()); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr01.creationTime().toInstant()); 

    File copy02 = testFolder.newFile("test-02.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-02.jpg"), copy02);
    tmc.syncTimes(copy02);
    BasicFileAttributes attr02 = Files.readAttributes(Paths.get(copy02.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2013-10-06T15:38:59Z"), attr02.lastModifiedTime().toInstant());     
    assertEquals(Instant.parse("2013-10-06T15:38:59Z"), attr02.lastAccessTime().toInstant()); 
    assertEquals(Instant.parse("2013-10-06T15:38:59Z"), attr02.creationTime().toInstant()); 

    File copy03 = testFolder.newFile("test-03.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-03.jpg"), copy03);    
    tmc.syncTimes(copy03);
    BasicFileAttributes attr03 = Files.readAttributes(Paths.get(copy03.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2013-10-06T15:38:50Z"), attr03.lastModifiedTime().toInstant()); 
    assertEquals(Instant.parse("2013-10-06T15:38:50Z"), attr03.lastAccessTime().toInstant()); 
    assertEquals(Instant.parse("2013-10-06T15:38:50Z"), attr03.creationTime().toInstant());         
  }

  @Test
  public void testMain_Help() {
    SyncFile2EXIF.main(new String[] { "help" });
  }

  @Test
  public void testMain_TimesNoFiles() {
    SyncFile2EXIF.main(new String[] { "times" });     
  }
  
  @Test
  public void testMain_Times() throws Exception {
    File copy01 = testFolder.newFile("test-01.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-01.jpg"), copy01);   
    File copy02 = testFolder.newFile("test-02.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-02.jpg"), copy02);    
    File copy03 = testFolder.newFile("test-03.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-03.jpg"), copy03);    

    SyncFile2EXIF.main(new String[] { "times", testFolder.getRoot().getAbsolutePath(), "*.jpg"});

    BasicFileAttributes attr01 = Files.readAttributes(Paths.get(copy01.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr01.lastModifiedTime().toInstant());    
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr01.lastAccessTime().toInstant()); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr01.creationTime().toInstant());         
    BasicFileAttributes attr02 = Files.readAttributes(Paths.get(copy02.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2013-10-06T15:38:59Z"), attr02.lastModifiedTime().toInstant()); 
    assertEquals(Instant.parse("2013-10-06T15:38:59Z"), attr02.lastAccessTime().toInstant()); 
    assertEquals(Instant.parse("2013-10-06T15:38:59Z"), attr02.creationTime().toInstant());         
    BasicFileAttributes attr03 = Files.readAttributes(Paths.get(copy03.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2013-10-06T15:38:50Z"), attr03.lastModifiedTime().toInstant());    
    assertEquals(Instant.parse("2013-10-06T15:38:50Z"), attr03.lastAccessTime().toInstant());   
    assertEquals(Instant.parse("2013-10-06T15:38:50Z"), attr03.creationTime().toInstant());             
  }
}


