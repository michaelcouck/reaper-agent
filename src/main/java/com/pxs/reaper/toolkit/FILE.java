package com.pxs.reaper.toolkit;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This class contains common utilities for files like deleting directories recursively, creating
 * files and directories and searching for files and directories recursively, before the {@link org.apache.commons.io.FileUtils}
 * from Apache was available.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25-01-2011
 */
public final class FILE {

    private static Logger log = Logger.getLogger(FILE.class.getSimpleName());

    /**
     * This method looks through all the files defined in the folder in the parameter
     * list, recursively, and gets the first one that matches the pattern.
     *
     * @param folder         the folder to start looking through
     * @param stringPatterns the patterns to look for in the file paths
     * @return the first file that was encountered that has the specified pattern(s) in it
     */
    public static File findFileRecursively(final File folder, final String... stringPatterns) {
        List<File> files = findFilesRecursively(folder, new ArrayList<>(), stringPatterns);
        return !files.isEmpty() ? files.get(0) : null;
    }

    /**
     * This method will look through all the files in the top level folder, and all
     * the sub folders, adding files to the list when they match the patterns that are provided.
     *
     * @param folder         the folder to start looking through
     * @param stringPatterns the patterns to match the file paths with
     * @param files          the files list to add all the files to
     * @return the list of files that match the patterns
     */
    private static List<File> findFilesRecursively(final File folder, final List<File> files, final String... stringPatterns) {
        if (folder != null && folder.isDirectory()) {
            File[] folderFiles = findFiles(folder, stringPatterns);
            if (folderFiles != null) {
                files.addAll(Arrays.asList(folderFiles));
            }
            File[] childFolders = folder.listFiles();
            if (childFolders != null) {
                for (final File childFolder : childFolders) {
                    findFilesRecursively(childFolder, files, stringPatterns);
                }
            }
        }
        return files;
    }

    /**
     * Finds files with the specified pattern only in the folder specified in the parameter list,
     * i.e. not recursively.
     *
     * @param folder         the folder to look for files in
     * @param stringPatterns the pattern to look for in the file path
     * @return an array of files with the specified pattern in the path
     */
    private static File[] findFiles(final File folder, final String... stringPatterns) {
        final Pattern pattern = getPattern(stringPatterns);
        return folder.listFiles(file -> {
            String pathName = file.getAbsolutePath();
            return pattern.matcher(pathName).matches();
        });
    }

    /**
     * Gets a single file. First looking to find it, if it can not be found then it is created.
     *
     * @param file the file that is requested
     * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static synchronized File getOrCreateFile(final File file) {
        try {
            if (file.exists() && file.isFile()) {
                return file;
            }
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.exists()) {
                    parent = getOrCreateDirectory(parent);
                }
            }
            if (parent != null) {
                try {
                    String parentPath = cleanFilePath(parent.getAbsolutePath());
                    File createdFile = new File(parentPath, file.getName());
                    log.fine("Creating file : " + file.getAbsolutePath());
                    createdFile.createNewFile();
                    return createdFile;
                } catch (final IOException e) {
                    log.log(Level.SEVERE, "Exception creating file : " + file, e);
                }
            }
            return file;
        } finally {
            FILE.class.notifyAll();
        }
    }

    /**
     * Gets a single directory. First looking to find it, if it can not be found then it is created.
     *
     * @param directory the directory that is requested
     * @return the found or newly created {@link File} or <code>null</code> if something went wrong.
     */
    private static synchronized File getOrCreateDirectory(final File directory) {
        try {
            if (directory.exists() && directory.isDirectory()) {
                return directory;
            }
            String directoryPath = cleanFilePath(directory.getPath());
            log.fine("Creating directory : " + directoryPath);
            File createdDirectory = new File(directoryPath);
            boolean created = createdDirectory.mkdirs();
            if (!created || !directory.exists()) {
                log.warning("Couldn't create directory(ies) " + directory.getAbsolutePath());
            }
            return createdDirectory;
        } finally {
            FILE.class.notifyAll();
        }
    }

    /**
     * Gets all the content from the file and puts it into a string,
     * assuming the default encoding for the platform and file contents are in fact strings.
     *
     * @param file the file to read into a string
     * @return the contents of the file or null if there was an exception reading the file
     */
    public static String getContent(final File file) {
        // FileInputStream fileInputStream = null;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            int read = fileInputStream.read(bytes);
            return new String(bytes, 0, read);
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Exception getting contents from file : " + file, e);
        }
        return "";
    }

    /**
     * Creates the pattern object from the regular expression patterns.
     *
     * @param stringPatterns the regular expression patterns
     * @return the pattern generated from the strings
     */
    private static Pattern getPattern(final String... stringPatterns) {
        boolean first = Boolean.TRUE;
        StringBuilder builder = new StringBuilder();
        for (String stringPattern : stringPatterns) {
            if (!first) {
                // Or
                builder.append("|");
            } else {
                first = Boolean.FALSE;
            }
            // Concatenate the 'any character' regular expression to the string pattern
            builder.append(".*(").append(stringPattern).append(").*");
        }
        return Pattern.compile(builder.toString());
    }

    private static void makeReadWrite(final File file) {
        boolean readable = file.setReadable(true, false);
        boolean writable = file.setWritable(true, false);
        if (!readable || !writable) {
            log.info("Didn't set file : " + file + ", readable : " + readable + ", writable : " + writable);
        }
    }

    /**
     * Writes the contents of a byte array to the file.
     *
     * @param outputFile the file to write to
     * @param bytes      the data to write to the file
     */
    public static void setContents(final File outputFile, final byte[] bytes) {
        FileOutputStream fileOutputStream = null;
        try {
            makeReadWrite(outputFile);
            fileOutputStream = new FileOutputStream(outputFile);
            setContents(fileOutputStream, bytes);
        } catch (final FileNotFoundException e) {
            log.log(Level.SEVERE, "File " + outputFile + " not found", e);
        } finally {
            close(fileOutputStream);
        }
    }

    private static void setContents(final OutputStream outputStream, final byte[] bytes) {
        try {
            outputStream.write(bytes, 0, bytes.length);
        } catch (final IOException e) {
            log.log(Level.SEVERE, "IO exception writing file contents", e);
        }
    }

    /**
     * Reads the contents of the file and returns the contents in a byte array form.
     *
     * @param inputStream the file to read the contents from
     * @param maxLength   the maximum number of bytes to read into the buffer
     * @return the file contents in a byte array output stream
     */
    public static ByteArrayOutputStream getContents(final InputStream inputStream, final long maxLength) {
        return getContents(inputStream, maxLength, Boolean.FALSE);
    }

    private static ByteArrayOutputStream getContents(final InputStream inputStream, final long maxLength, final boolean close) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (inputStream == null) {
            return byteArrayOutputStream;
        }
        try {
            byte[] bytes = new byte[1024];
            int read = inputStream.read(bytes);
            while (read > -1 && byteArrayOutputStream.size() < maxLength) {
                byteArrayOutputStream.write(bytes, 0, read);
                read = inputStream.read(bytes);
            }
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Exception accessing the file contents : " + inputStream, e);
        } finally {
            if (close) {
                close(inputStream);
            }
        }
        return byteArrayOutputStream;
    }

    public static void close(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Exception closing stream : " + inputStream, e);
        }
    }

    private static void close(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.flush();
            } catch (final Exception e) {
                log.log(Level.SEVERE, "Exception flushing stream : " + outputStream, e);
            }
            try {
                outputStream.close();
            } catch (final Exception e) {
                log.log(Level.SEVERE, "Exception closing stream : " + outputStream, e);
            }
        }
    }

    /**
     * This method will clean the path, as some operating systems add their special
     * characters, back spaces and the like, that interfere with the normal working of the
     * file system.
     *
     * @param path the path to clea, perhaps something like 'file:C:\\path\\.\\some\\more'
     * @return the path that can be used as an absolute path on the file system
     */
    public static String cleanFilePath(final String path) {
        String filePath = path;
        filePath = StringUtils.replace(filePath, "/./", "/");
        // For windows we must clean the path of 'file:/' because getting the
        // parent then appends the user path for some reason too, returning something
        // like C:/tmp/user/directory/C:/path/to/directory
        filePath = StringUtils.replace(filePath, "file:", "");
        filePath = StringUtils.replace(filePath, "file:/", "");
        filePath = StringUtils.replace(filePath, "file:\\", "");
        filePath = StringUtils.replace(filePath, "\\.\\", "/");
        filePath = StringUtils.replace(filePath, "\\", "/");
        filePath = StringUtils.removeEnd(filePath, ".");
        return filePath;
    }

    /**
     * Singularity.
     */
    private FILE() {
        // Documented
    }

}