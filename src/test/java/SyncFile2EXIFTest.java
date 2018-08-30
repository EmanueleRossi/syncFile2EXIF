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
  public void testSample01() throws Exception {
    File copy = testFolder.newFile("test-01.jpg");  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-01.jpg"), copy);

    tmc.syncTimes(copy);

    BasicFileAttributes attr = Files.readAttributes(Paths.get(copy.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr.creationTime().toInstant()); 
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
    File copy = testFolder.newFile("test-02.jpg"); 
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-01.jpg"), copy);    

    SyncFile2EXIF.main(new String[] { "times", copy.getParentFile().getAbsolutePath(), "*.jpg"});

    BasicFileAttributes attr = Files.readAttributes(Paths.get(copy.getAbsolutePath()), BasicFileAttributes.class); 
    assertEquals(Instant.parse("2016-07-10T14:53:51Z"), attr.creationTime().toInstant());     
  }
}


