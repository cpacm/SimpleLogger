package com.cpacm.log.transform;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.gradle.api.Project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * <p>
 * 添加android jar 到 classloader 中
 *
 * @author cpacm 2019-11-06
 */
public class AndroidClassLoader {

    public static URLClassLoader getClassLoader(Collection<TransformInput> inputs,
                                                Collection<TransformInput> referencedInputs,
                                                Project project) throws MalformedURLException {
        ImmutableList.Builder<URL> urls = new ImmutableList.Builder<>();
        String androidJarPath = getAndroidJarPath(project);
        if (androidJarPath != null) {
            File file = new File(androidJarPath);
            URL androidJarURL = file.toURI().toURL();
            urls.add(androidJarURL);
            for (TransformInput totalInputs : Iterables.concat(inputs, referencedInputs)) {
                for (DirectoryInput directoryInput : totalInputs.getDirectoryInputs()) {
                    if (directoryInput.getFile().isDirectory()) {
                        urls.add(directoryInput.getFile().toURI().toURL());
                    }
                }
                for (JarInput jarInput : totalInputs.getJarInputs()) {
                    if (jarInput.getFile().isFile()) {
                        urls.add(jarInput.getFile().toURI().toURL());
                    }
                }
            }
        }
        ImmutableList<URL> allUrls = urls.build();
        URL[] classLoaderUrls = allUrls.toArray(new URL[allUrls.size()]);
        return new URLClassLoader(classLoaderUrls);
    }

    /**
     * /Users/cpacm/Documents/Android/SDK/platforms/android-28/android.jar
     */
    private static String getAndroidJarPath(Project project) {
        if (project.getProperties().get("android") instanceof AppExtension) {
            AppExtension appExtension = (AppExtension) project.getProperties().get("android");
            String sdkDirectory = appExtension.getSdkDirectory().getAbsolutePath();
            String compileSdkVersion = appExtension.getCompileSdkVersion();
            sdkDirectory = sdkDirectory + File.separator + "platforms" + File.separator;
            return sdkDirectory + compileSdkVersion + File.separator + "android.jar";
        } else if (project.getProperties().get("android") instanceof LibraryExtension) {
            LibraryExtension appExtension = (LibraryExtension) project.getProperties().get("android");
            String sdkDirectory = appExtension.getSdkDirectory().getAbsolutePath();
            String compileSdkVersion = appExtension.getCompileSdkVersion();
            sdkDirectory = sdkDirectory + File.separator + "platforms" + File.separator;
            return sdkDirectory + compileSdkVersion + File.separator + "android.jar";
        }
        return null;
    }

}
