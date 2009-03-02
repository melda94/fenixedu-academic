package net.sourceforge.fenixedu.domain;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.activation.MimetypesFileTypeMap;

import net.sourceforge.fenixedu.domain.accessControl.EveryoneGroup;
import net.sourceforge.fenixedu.domain.accessControl.Group;
import net.sourceforge.fenixedu.injectionCode.AccessControl;
import net.sourceforge.fenixedu.presentationTier.Action.publico.FileDownload;

import org.joda.time.DateTime;

import pt.ist.fenixframework.pstm.Transaction;
import pt.utl.ist.fenix.tools.file.FileDescriptor;
import pt.utl.ist.fenix.tools.file.FileManagerFactory;
import pt.utl.ist.fenix.tools.file.FileSetMetaData;
import pt.utl.ist.fenix.tools.file.VirtualPath;
import pt.utl.ist.fenix.tools.util.FileUtils;

public abstract class File extends File_Base {

    protected File() {
	super();
	setRootDomainObject(RootDomainObject.getInstance());
    }

    protected void init(VirtualPath path, String filename, String displayName, Collection<FileSetMetaData> metadata,
	    byte[] content, Group group) {
	setFilename(FileUtils.getFilenameOnly(filename));
	setDisplayName(FileUtils.getFilenameOnly(displayName));
	new FileLocalContent(this, path, metadata, content);
	setMimeType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename));
	setSize(content.length);
	setPermittedGroup(group);
	setUploadTime(new DateTime());
    }

    public void storeToContentManager() {
	final FileDescriptor fileDescriptor = FileManagerFactory.getFactoryInstance().getFileManager().saveFile(
		getLocalContent().getPath(), getFilename(), isPrivate(), getLocalContent().createMetadata(),
		new ByteArrayInputStream(getLocalContent().getContent().getBytes()));
	setMimeType(fileDescriptor.getMimeType());
	setChecksum(fileDescriptor.getChecksum());
	setChecksumAlgorithm(fileDescriptor.getChecksumAlgorithm());
	setSize(fileDescriptor.getSize());
	setExternalStorageIdentification(fileDescriptor.getUniqueId());
    }

    public boolean isPrivate() {
	if (getPermittedGroup() == null)
	    return false;
	if (getPermittedGroup() instanceof EveryoneGroup)
	    return false;
	return true;
    }

    public InputStream getStream() {
	if (hasLocalContent())
	    return new ByteArrayInputStream(getLocalContent().getContent().getBytes());
	return FileManagerFactory.getFactoryInstance().getFileManager().retrieveFile(getExternalStorageIdentification());
    }

    public byte[] getContents() {
	return getLocalContent().getContent().getBytes();
    }

    /**
     * @return returns a public url that can be used by a client to download the
     *         associated file from the external file storage
     */
    public String getDownloadUrl() {
	if (hasLocalContent())
	    return FileDownload.ACTION_PATH + getIdInternal();
	return FileManagerFactory.getFactoryInstance().getFileManager().formatDownloadUrl(getExternalStorageIdentification(),
		getFilename());
    }

    public void delete() {
	if (hasLocalContent()) {
	    getLocalContent().delete();
	} else {
	    new DeleteFileRequest(AccessControl.getPerson(), getExternalStorageIdentification());
	}
	removeRootDomainObject();
	deleteDomainObject();
    }

    public boolean isPersonAllowedToAccess(Person person) {
	final Group group = this.getPermittedGroup();
	return group == null || group.isMember(person);
    }

    // -------------------------------------------------------------
    // read static methods
    // -------------------------------------------------------------
    public static File readByExternalStorageIdentification(String externalStorageIdentification) {
	// For performance reasons...
	PreparedStatement stmt = null;
	try {
	    final Connection connection = Transaction.getCurrentJdbcConnection();
	    stmt = connection.prepareStatement("SELECT ID_INTERNAL FROM FILE WHERE EXTERNAL_STORAGE_IDENTIFICATION = ?");

	    stmt.setString(1, externalStorageIdentification);
	    final ResultSet resultSet = stmt.executeQuery();
	    if (resultSet.next()) {
		return RootDomainObject.getInstance().readFileByOID(resultSet.getInt(1));
	    }

	    return null;
	} catch (SQLException e) {
	    throw new Error(e);
	} finally {
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    throw new Error(e);
		}
	    }
	}
    }

    // OLD PORTIONS

    /**
     * @use {@link File#init(VirtualPath, String, String, Collection, byte[], Group)}
     */
    @Deprecated
    protected void init(String filename, String displayName, String mimeType, String checksum, String checksumAlgorithm,
	    Integer size, String externalStorageIdentification, Group permittedGroup) {
	setFilename(FileUtils.getFilenameOnly(filename));
	setDisplayName(FileUtils.getFilenameOnly(displayName));
	setMimeType(mimeType);
	setChecksum(checksum);
	setChecksumAlgorithm(checksumAlgorithm);
	setSize(size);
	setExternalStorageIdentification(externalStorageIdentification);
	setPermittedGroup(permittedGroup);
	setUploadTime(new DateTime());
    }

}
