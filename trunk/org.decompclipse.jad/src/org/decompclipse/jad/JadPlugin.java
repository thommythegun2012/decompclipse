package org.decompclipse.jad;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JadPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.decompclipse.jad";

	public static final String PREF_TMP_DIR = "org.decompclipse.jad.tmpdir";
	public static final String PREF_CMD = "org.decompclipse.jad.cmd";
	public static final String PREF_ALIGN = "org.decompclipse.jad.align";
	public static final String PREF_REUSE_BUFFER = "org.decompclipse.jad.reusebuff";
	public static final String PREF_IGNORE_EXISTING = "org.decompclipse.jad.alwaysuse";

	// The shared instance
	private static JadPlugin plugin;

	public JadPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static JadPlugin getDefault() {
		return plugin;
	}

	public static void logError(Throwable t, String message) {
		getDefault().getLog().log(
				new Status(Status.ERROR, PLUGIN_ID, 0, message, t));
	}

	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(PREF_CMD, "jad.exe");
		store.setDefault(PREF_TMP_DIR, getDefaultTempDir());
		store.setDefault(PREF_REUSE_BUFFER, true); // since 2.02
		store.setDefault(PREF_IGNORE_EXISTING, false);
		store.setDefault(IJadOptions.OPTION_INDENT_SPACE, 4);
		store.setDefault(IJadOptions.OPTION_IRADIX, 10);
		store.setDefault(IJadOptions.OPTION_LRADIX, 10);
		store.setDefault(IJadOptions.OPTION_SPLITSTR_MAX, 0); // disable
		store.setDefault(IJadOptions.OPTION_PI, 0); // disable
		store.setDefault(IJadOptions.OPTION_PV, 0); // disable
	}
	
	public String getDefaultTempDir(){
		try {
			File tempFile = File.createTempFile("tempFile_"+System.currentTimeMillis(), ".txt");
			return tempFile.getParent();
		} catch (IOException e) {
			JadPlugin.logError(e, "Failed to get temp directory");
		}
		return "";
	}

	public String getTmpDir() {
		return getPreferenceStore().getString(JadPlugin.PREF_TMP_DIR);

	}

}
