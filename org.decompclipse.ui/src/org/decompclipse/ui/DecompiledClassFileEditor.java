package org.decompclipse.ui;

import java.util.Map;


import org.decompclipse.core.DecompiledSourceMapper;
import org.decompclipse.core.DecompilerType;
import org.decompclipse.core.IDecompiler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Subclass of standard ClassFileEditor which hijacks the source mapper to
 * decompile class files which don't have a source attachment. This means that
 * instead of presenting bytecode for the methods, the selected class is
 * decompiled on the fly with a customizable decompiler. The rest of the
 * functionality of ClassFileEditor remains untouched.
 * 
 * @author Johann Gyger
 */
public class DecompiledClassFileEditor extends ClassFileEditor {

	private IClassFileEditorInput classEditorInput;

	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(
					JdtDecompilerUiPlugin.PREF_DECOMPILER)) {
				updateDecompilerInSourceMapper(JdtDecompilerUiPlugin
						.getPreferredDecompiler());
			}
		}
	};

	protected IEditorInput transformEditorInput(IEditorInput input) {
		input = super.transformEditorInput(input);
		if (input instanceof IClassFileEditorInput) {
			classEditorInput = (IClassFileEditorInput) input;
			IPackageFragmentRoot root = extractPackageFragmentRoot();
			if (root != null) {
				hijackSourceMapper((IPackageFragmentRoot) root);
			}
		}

		return input;
	}
	
	private PackageFragmentRoot extractPackageFragmentRoot() {
		if (classEditorInput == null) {
			return null;
		}
		IClassFile file = classEditorInput.getClassFile();
		IJavaElement element = file
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		if (element instanceof PackageFragmentRoot) {
			return (PackageFragmentRoot) element;
		}

		return null;
	}

	/*
	 * @see AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
	 */
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);

		MenuManager decompilerMenu = new MenuManager("Decompile With");

		DecompilerType[] types = DecompilerType.getTypes();
		for (int i = 0; i < types.length; i++) {
			DecompilerType type = types[i];
			decompilerMenu.add(new DecompileWithAction(this, type));
		}
		decompilerMenu
				.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS,
				decompilerMenu);
	}

	private void hijackSourceMapper(IPackageFragmentRoot root) {
		try {
			SourceMapper mapper = ((PackageFragmentRoot)root).getSourceMapper();
			JdtDecompilerUiPlugin.getDefault().getPluginPreferences()
					.addPropertyChangeListener(propertyChangeListener);
			if (!(mapper instanceof DecompiledSourceMapper)) {
				IDecompiler decompiler = JdtDecompilerUiPlugin
						.getPreferredDecompiler();

				IPath sourcePath;
				sourcePath = root.getSourceAttachmentPath();
				if (sourcePath == null) {
					sourcePath = root.getPath(); // attach root to itself
				}

				String rootPath = null;
				IPath iRootPath = root.getSourceAttachmentRootPath();
				if (iRootPath != null) {
					rootPath = iRootPath.toOSString();
				}

				Map options = root.getJavaProject().getOptions(true);

				DecompiledSourceMapper newMapper = new DecompiledSourceMapper(
						sourcePath, rootPath, options, root, decompiler);
				((PackageFragmentRoot)root).setSourceMapper(newMapper);
			}
		} catch (JavaModelException e) {
			JdtDecompilerUiPlugin.logError(e,
					"Source mapper could not be hijacked for package fragment root "
							+ root.getPath());
		}
	}

	private void updateDecompilerInSourceMapper(IDecompiler decompiler) {
		PackageFragmentRoot root = extractPackageFragmentRoot();
		if (root == null) {
			return;
		}

		SourceMapper sourceMapper = root.getSourceMapper();
		if (sourceMapper instanceof DecompiledSourceMapper) {
			System.out.println("DecompiledSourceMapper");
			DecompiledSourceMapper decompiledMapper = (DecompiledSourceMapper) sourceMapper;
			decompiledMapper.setDecompiler(decompiler);
		}
	}

	public void forceDecompile() {
		try {
			classEditorInput.getClassFile().close();
			inputChanged(classEditorInput);
		} catch (Exception e) {
			JdtDecompilerUiPlugin.logError(e, "Could not force a recompile.");
		}
	}

	public void decompileWith(IDecompiler decompiler) {
		try {
			updateDecompilerInSourceMapper(decompiler);
			forceDecompile();
		} catch (Exception e) {
			JdtDecompilerUiPlugin.logError(e, "Could not decompile with: "
					+ decompiler);
		}
	}

	public void dispose() {
		JdtDecompilerUiPlugin.getDefault().getPluginPreferences()
				.removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}

}
