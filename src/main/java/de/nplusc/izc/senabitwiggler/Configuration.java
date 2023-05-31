package de.nplusc.izc.senabitwiggler;

public class Configuration {
    private String BlueLabPath;
    private String SoxPath;

    private boolean MatrixEnabled = false;

    private String MatrixUser;

    private String MatrixPassword;

    private String MatrixDomain;

    private String ArchiveOrgCollection;

    public String getMatrixDomain() {
        return MatrixDomain;
    }

    public void setMatrixDomain(String matrixDomain) {
        MatrixDomain = matrixDomain;
    }

    public boolean isMatrixEnabled() {
        return MatrixEnabled;
    }

    public String getMatrixPassword() {
        return MatrixPassword;
    }

    public String getMatrixUser() {
        return MatrixUser;
    }

    public void setMatrixEnabled(boolean matrixEnabled) {
        MatrixEnabled = matrixEnabled;
    }

    public void setMatrixPassword(String matrixPassword) {
        MatrixPassword = matrixPassword;
    }

    public void setMatrixUser(String matrixUser) {
        MatrixUser = matrixUser;
    }

    public String getBlueLabPath() {
        return BlueLabPath;
    }

    public void setBlueLabPath(String blueLabPath) {
        BlueLabPath = blueLabPath;
    }

    public String getSoxPath() {
        return SoxPath;
    }

    public void setSoxPath(String soxPath) {
        SoxPath = soxPath;
    }

    public String getArchiveOrgCollection() {
        return ArchiveOrgCollection;
    }

    public void setArchiveOrgCollection(String archiveOrgCollection) {
        ArchiveOrgCollection = archiveOrgCollection;
    }
}
