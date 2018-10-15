import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

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
    tmc.setTimes(new File(".\\build\\resources\\test\\NonExistentFile.jpg"), new Date());
  }

  @Test
  public void testNonImageFile() {    
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(outContent));

    tmc.setTimes(new File(".\\build\\resources\\test\\sample-02.txt"), new Date());
    assertNotNull(outContent);
  }  

  @Test
  public void testSamples() throws Exception {
    File copy01 = testFolder.newFile("sample-01.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-01.jpg"), copy01);
    tmc.setTimes(copy01, tmc.getEXIFOriginalDateTime(copy01));
    BasicFileAttributes attr01 = Files.readAttributes(Paths.get(copy01.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr01.lastModifiedTime().toInstant()); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr01.lastAccessTime().toInstant()); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr01.creationTime().toInstant()); 

    File copy02 = testFolder.newFile("sample-02.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-02.jpg"), copy02);
    tmc.setTimes(copy02, tmc.getEXIFOriginalDateTime(copy02));
    BasicFileAttributes attr02 = Files.readAttributes(Paths.get(copy02.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2013-10-06T15:38:59Z"), attr02.lastModifiedTime().toInstant());     
    assertEquals(Instant.parse("2013-10-06T15:38:59Z"), attr02.lastAccessTime().toInstant()); 
    assertEquals(Instant.parse("2013-10-06T15:38:59Z"), attr02.creationTime().toInstant()); 

    File copy03 = testFolder.newFile("sample-03.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-03.jpg"), copy03);    
    tmc.setTimes(copy03, tmc.getEXIFOriginalDateTime(copy03));
    BasicFileAttributes attr03 = Files.readAttributes(Paths.get(copy03.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2013-10-06T15:38:50Z"), attr03.lastModifiedTime().toInstant()); 
    assertEquals(Instant.parse("2013-10-06T15:38:50Z"), attr03.lastAccessTime().toInstant()); 
    assertEquals(Instant.parse("2013-10-06T15:38:50Z"), attr03.creationTime().toInstant());         
  }

  @Test
  public void testMain_Help() {
    ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(sysOut));

    SyncFile2EXIF.main(new String[] { "help" });

    assertTrue(sysOut.toString().contains("Usage:"));
  }

  @Test
  public void testMain_TimesNoFiles() {
    SyncFile2EXIF.main(new String[] { "times" });     
  }
  
  @Test
  public void testMain_Times() throws Exception {
    File copy01 = testFolder.newFile("sample-01.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-01.jpg"), copy01);   
    File copy02 = testFolder.newFile("sample-02.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-02.jpg"), copy02);    
    File copy03 = testFolder.newFile("sample-03.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-03.jpg"), copy03);    

    SyncFile2EXIF.main(new String[] { "times", testFolder.getRoot().getAbsolutePath(), "*.jpg"} );

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

  @Test
  public void testMain_Name() throws Exception {
    File copy01 = testFolder.newFile("sample-01.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-01.jpg"), copy01);       

    SyncFile2EXIF.main(new String[] { "name", testFolder.getRoot().getAbsolutePath(), copy01.getName() });

    File outputFile = Arrays.stream(testFolder.getRoot().listFiles()).findFirst().get();
    assertTrue(outputFile.getName().equalsIgnoreCase("20160710165351.jpg"));
  }

  @Test
  public void testMain_NameDuplicated() throws Exception {
    File copy01 = testFolder.newFile("sample-04-01.jpg");  
    File copy02 = testFolder.newFile("sample-04-02.jpg");      
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-04.jpg"), copy01);   
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-04.jpg"), copy02);         

    SyncFile2EXIF.main(new String[] { "name", testFolder.getRoot().getAbsolutePath(), copy01.getName() });
    SyncFile2EXIF.main(new String[] { "name", testFolder.getRoot().getAbsolutePath(), copy02.getName() });    

    assertEquals(2, Arrays.stream(testFolder.getRoot().listFiles()).count());
  }  

  @Test
  public void testMain_SetTimeNoExif() throws Exception {
    File copy01 = testFolder.newFile("20100919-Sonia-006.jpg");     
    FileUtils.copyFile(new File(".\\build\\resources\\test\\20100919-Sonia-006.jpg"), copy01);           

    SyncFile2EXIF.main(new String[] { "setTimes", testFolder.getRoot().getAbsolutePath(), copy01.getName(), "20100919030000" });       

    BasicFileAttributes attr01 = Files.readAttributes(Paths.get(copy01.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2010-09-19T03:00:00Z"), attr01.lastModifiedTime().toInstant());    
    assertEquals(Instant.parse("2010-09-19T03:00:00Z"), attr01.lastAccessTime().toInstant()); 
    assertEquals(Instant.parse("2010-09-19T03:00:00Z"), attr01.creationTime().toInstant());    
  }
}
