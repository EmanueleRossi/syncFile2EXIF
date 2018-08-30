import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

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
  public void testSample01() throws IOException {

    //File copy = testFolder.newFile("test-01.jpg");
    File copy = new File(".\\build\\resources\\test\\test-012.jpg");
  
    FileUtils.copyFile(new File(".\\build\\resources\\test\\sample-01.jpg"), copy);
    tmc.syncTimes(copy);
  }
}


