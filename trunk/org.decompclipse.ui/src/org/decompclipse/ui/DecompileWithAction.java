package org.decompclipse.ui;


import org.decompclipse.core.DecompilerType;
import org.decompclipse.core.IDecompiler;
import org.eclipse.jface.action.Action;

class DecompileWithAction extends Action {

	private final DecompiledClassFileEditor editor;
	
	private final DecompilerType decompilerType;

	public DecompileWithAction(DecompiledClassFileEditor editor, DecompilerType decompilerType) {
		super(decompilerType.getName());
		this.editor = editor;
		this.decompilerType = decompilerType;
	}

	public void run() {
		if (editor != null) {
			IDecompiler decompiler = decompilerType.getDecompiler();
			editor.decompileWith(decompiler);
		}
	}
}
