package dev.vectorpatch.service;

interface IVectorPatchService {
    void installApk(String apkPath) = 1;
    void uninstallPackage(String packageName) = 2;
    boolean isPackageInstalled(String packageName) = 3;
    List<String> getPatchableApps() = 4;
    void destroy() = 16777114;
}
