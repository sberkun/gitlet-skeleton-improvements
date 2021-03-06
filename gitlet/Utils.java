package gitlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;


/** Assorted utilities.
 *
 * Give this file a good read as it provides several useful utility functions
 * to save you some time.
 *
 *  Original @author P. N. Hilfinger
 *  Updates by Samuel Berkun
 */
class Utils {

    /* SHA-1 HASH VALUES. */

    /** The length of a complete SHA-1 UID as a hexadecimal numeral. */
    static final int UID_LENGTH = 40;

    /** Returns the SHA-1 hash of VAL. To take the SHA-1 of
     *  an object, serialize() the object first.
     *  If you want to hash multiple things together,
     *  use the concat() function. */
    static String sha1(byte[] val) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            Formatter result = new Formatter();
            for (byte b : md.digest(val)) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /** Overload of sha1 function above. */
    static String sha1(String val) {
        return sha1(val.getBytes(StandardCharsets.UTF_8));
    }

    /* SERIALIZATION UTILITIES */

    /** Concatenates several byte[] or Strings into one byte[], which
     *  can then be used in sha1() or writeContents()
     *  Example usage: sha1(concat(fileContentBytes, "I like cheese"))
     *  Example usage: writeContents(file, concat("cool beans", moreBytes)) */
    static byte[] concat(Object... values) {
        int totalLength = 0;
        for (int a = 0; a < values.length; a++) {
            if (values[a] instanceof String) {
                byte[] asBytes = ((String) values[a]).getBytes(StandardCharsets.UTF_8);
                values[a] = asBytes;
                totalLength += asBytes.length;
            } else if (values[a] instanceof byte[]) {
                totalLength += ((byte[]) values[a]).length;
            } else if (values[a] == null) {
                throw new IllegalArgumentException("Cannot concatenate null");
            } else {
                throw new IllegalArgumentException("Utils.concat() can"
                        + " only be used on byte[] or Strings. For general objects,"
                        + " please serialize() them first.");
            }
        }
        byte[] result = new byte[totalLength];
        int start = 0;
        for (Object val : values) {
            if (val instanceof byte[]) { //always true, this is so it doesn't make warnings
                System.arraycopy(val, 0, result, start, ((byte[]) val).length);
                start += ((byte[]) val).length;
            }
        }
        return result;
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     *
     *  You are not allowed to call serialize(file) or writeObject(dest, file),
     *  since that serializes the PATH of the file rather than the file itself.
     *  If you want to take the sha1 of a file, for example, what you really
     *  want to do is sha1(readContents(file))
     *  If you you do indeed want to save or sha1 the path of a file, you can do
     *  writeString(file.getPath()), or sha1(file.getPath()) */
    static byte[] serialize(Serializable obj) {
        if (obj instanceof File) {
            throw new IllegalArgumentException("Please do not call serialize or writeObject on"
                + " File objects! You're most likely looking for readContents or copyFile."
                + " See the comments of Utils.serialize for more details.");
        }
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "Serialization error. Please make sure this class is Serializable: "
                            + ex.getMessage());
        }
    }

    /* FILE DELETION */

    /** Deletes FILE if it exists and is not a directory.  Returns true
     *  if FILE was deleted, and false otherwise.  Refuses to delete FILE
     *  and throws IllegalArgumentException unless the directory designated by
     *  FILE also contains a directory named .gitlet, or file is in .gitlet.
     *
     *  Note: Despite the name, safeDelete is not entirely safe. It can still
     *  wipe out the files of whichever directory you run gitlet in. */
    static boolean safeDelete(File file) {
        boolean isInGitlet = file.getPath().contains(".gitlet");
        boolean isInGitletCWD = (new File(file.getParentFile(), ".gitlet")).isDirectory();
        if (!isInGitlet && !isInGitletCWD) {
            throw new IllegalArgumentException(
                    "File to be deleted is not in .gitlet "
                            + "or in .gitlet working directory: " + file.getPath());
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("File to be deleted is a directory: "
                    + file.getPath());
        }
        return file.delete();
    }

    /* FILE COPYING */

    /** Copies a file from SOURCE to DESTINATION. Will overwrite destination
     *  file if it exists. Throws an exception if the source doesn't exist,
     *  or if either file is a directory. */
    static void copyFile(File source, File destination) {
        if (!source.exists()) {
            throw new IllegalArgumentException("Source file does not exist: " + source.getPath());
        }
        if (source.isDirectory()) {
            throw new IllegalArgumentException("Source file is a directory: " + source.getPath());
        }
        if (destination.isDirectory()) {
            throw new IllegalArgumentException(
                    "Destination file is a directory: " + destination.getPath());
        }
        try {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    /* READING AND WRITING FILE CONTENTS */

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  not be a directory.  Throws IllegalArgumentException
     *  in case of problems. */
    static byte[] readContents(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            if (!file.exists()) {
                throw new IllegalArgumentException("File does not exist: " + file.getPath());
            }
            if (file.isDirectory()) {
                throw new IllegalArgumentException("File is a directory: " + file.getPath());
            }
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    /** Write the bytes in CONTENTS to FILE, creating or overwriting it as
     *  needed. If you want to write multiple things together, use the
     *  concat() function */
    static void writeContents(File file, byte[] contents) {
        try {
            BufferedOutputStream str =
                    new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            str.write(contents);
            str.close();
        } catch (IOException ex) {
            if (file.isDirectory()) {
                throw new IllegalArgumentException("Cannot overwrite directory: "
                        + file.getPath());
            }
            if (!file.getParentFile().exists()) {
                throw new IllegalArgumentException("File's parent directory does not exist: "
                        + file.getPath());
            }
            if (!file.getParentFile().isDirectory()) {
                throw new IllegalArgumentException("File's parent is not a directory: "
                        + file.getPath());
            }
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    /** Like readContents, but returns a String. I would avoid using this
     *  for arbitrary CWD files, since if the file is not txt (like an
     *  image file), this gives you garbage. */
    static String readString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    /** Like writeContents, but writes a String. */
    static void writeString(File file, String contents) {
        writeContents(file, contents.getBytes(StandardCharsets.UTF_8));
    }

    /** Return an object of type T read from FILE, casting it to EXPECTEDCLASS.
     *  Similar to readContents. */
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException ex) {
            String exMessage = ex.getMessage();
            if (exMessage.startsWith("invalid stream header")) {
                throw new IllegalArgumentException("You're reading with readObject, "
                        + "but the file was not written with writeObject. Please check that "
                        + "this is the right file.\n" + exMessage);
            }
            if (exMessage.contains("local class incompatible")) {
                throw new IllegalArgumentException(exMessage
                        + "\nTypically deleting the .gitlet directory and restarting with"
                        + " gitlet init will fix this error.");
            }
            throw new IllegalArgumentException(ex.getMessage());
        } catch (ClassCastException | ClassNotFoundException ex) {
            throw new IllegalArgumentException("ClassCastException: " + ex.getMessage());
        }
    }

    /** Write OBJ to FILE; similar to writeContents. */
    static void writeObject(File file, Serializable obj) {
        writeContents(file, serialize(obj));
    }

    /* DIRECTORIES */

    /** Returns an array of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings. Errors if DIR does
     *  not denote a directory. */
    static String[] plainFilenamesIn(File dir) {
        String[] files = dir.list((d, name) -> new File(d, name).isFile());
        if (files == null) {
            throw new IllegalArgumentException(
                    "File is not a directory, so cannot list the files in it");
        } else {
            Arrays.sort(files);
            return files;
        }
    }

    /* OTHER FILE UTILITIES */

    /** Return the concatenation of FIRST and OTHERS into a File designator,
     *  analogous to the java.nio.file.Paths.get(String, String[])
     *  method. */
    static File join(String first, String... others) {
        try {
            return Paths.get(first, others).toFile();
        } catch (NullPointerException ex) {
            StringBuilder msg = new StringBuilder("One of the arguments to join is null: (");
            if (first == null) {
                msg.append("null");
            } else {
                msg.append('"').append(first).append('"');
            }
            for (String str : others) {
                if (str == null) {
                    msg.append(", null");
                } else {
                    msg.append(", \"").append(str).append('"');
                }
            }
            msg.append(")");
            throw new IllegalArgumentException(msg.toString());
        }
    }

    /** Return the concatenation of FIRST and OTHERS into a File designator,
     *  analogous to the java.nio.file.Paths.#get(String, String[])
     *  method. */
    static File join(File first, String... others) {
        return join(first.getPath(), others);
    }


    /* ERROR REPORTING */

    /** Return a GitletException with the given message.
     *  The intended use is for you to throw error(msg) when Gitlet
     *  encounters an error, and then to catch the GitletException in Main. */
    static GitletException error(String msg) {
        return new GitletException(msg);
    }

}
