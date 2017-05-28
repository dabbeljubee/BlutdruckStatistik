package de.dabbeljubee.blutdruckstatistik.Tools;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import de.dabbeljubee.blutdruckstatistik.R;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FileAccess {
    private static  final Logger LOGGER = Logger.getLogger("FileAccess");

    public StringBuffer readFromInternalFile(Context context, String fileName) throws IOException {
        return readFromFile(new File(context.getFilesDir(), fileName));
    }

    public StringBuffer readFromExternalFile(String fileName) throws IOException {
        return readFromFile(getExternalFile(fileName));
    }

    public List<String> readFromExternalFileByLine(String fileName) throws IOException {
        boolean state = isExternalStorageWritable();
        LOGGER.info(String.format("External storage mounted: %b", state));
        final String absolutePath = getExternalFile(fileName).getAbsolutePath();
        LOGGER.info(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        LOGGER.info(absolutePath);
        FileReader fileReader = new FileReader(absolutePath);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<>();
        String line;
        while (null != (line = bufferedReader.readLine())) {
            LOGGER.info(line);
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

    private File getExternalFile(String fileName) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
//        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/../" + fileName);
    }

    private StringBuffer readFromFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);

        StringBuffer fileContent = new StringBuffer("");
        byte[] buffer = new byte[1024];

        int n;
        while ((n = fileInputStream.read(buffer)) != -1) {
            fileContent.append(new String(buffer, 0, n));
        }
        LOGGER.fine(fileContent.toString());
        return fileContent;
    }

    public void saveToExternalFile(Context context, String fileName, String content) {
        try {
            final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            final File externalFile = new File(path, fileName);
            saveToFile(externalFile, content);
            boolean isReadable = externalFile.setReadable(true);
            if (! isReadable) {
                LOGGER.fine("File is not readable");
            }
            new MediaScannerHelper().addFile(context, externalFile.getCanonicalPath());
            Toast.makeText(context, context.getResources().getString(R.string.stored_file, externalFile.getCanonicalPath()), Toast.LENGTH_LONG)
                    .show();
        } catch (Exception e) {
            Toast.makeText(context, context.getResources().getString(R.string.cannot_store_file, fileName, e.getMessage()), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public void saveToInternalFile(Context context, String fileName, String content) {
        saveToFile(new File(context.getFilesDir(), fileName), content);
    }

    private void saveToFile(File file, String content) {
        LOGGER.fine(String.format("Access file %s%s", file.getAbsolutePath(), file.getName()));

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            LOGGER.severe("Output file not found");
        } catch (IOException ioe) {
            LOGGER.severe("Error writing file");
        }
    }

    public class MediaScannerHelper implements MediaScannerConnection.MediaScannerConnectionClient {

        void addFile(Context context, String filename)
        {
            String [] paths = new String[1];
            paths[0] = filename;
            MediaScannerConnection.scanFile(context, paths, null, this);
        }

        public void onMediaScannerConnected() {
        }

        public void onScanCompleted(String path, Uri uri) {
            LOGGER.fine("Scan done - path:" + path + " uri:" + uri);
        }
    }
}