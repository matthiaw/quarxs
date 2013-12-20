package org.rogatio.quarxs;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.rogatio.quarxs.util.Constants;
import org.xwiki.component.annotation.Component;
import org.xwiki.sheet.SheetBinder;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.mandatory.AbstractMandatoryDocumentInitializer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.sheet.SheetBinder;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.BooleanClass;

@Component
@Named(Node.CLASS+"Initializer")
@Singleton
public class NodeClassInitializer extends AbstractMandatoryDocumentInitializer {

	/**
	 * Default list separators of Graph.GraphClass fields.
	 */
	public static final String DEFAULT_FIELDS = "|";
	
	public static final String FIELD_LABEL = "label";
	public static final String FIELDPN_LABEL = "Label";
	
	public static final String FIELD_NODETYPE = "nodetype";
	public static final String FIELDPN_NODETYPE = "Type of Node";
	
	public static final String FIELD_PRETTYID = "prettyid";
	public static final String FIELDPN_PRETTYID = "Humane Readable Id";
	
	/**
	 * Used to bind a class to a document sheet.
	 */
	@Inject
	@Named("class")
	private SheetBinder classSheetBinder;

	/**
	 * Used to access current XWikiContext.
	 */
	@Inject
	private Provider<XWikiContext> xcontextProvider;

	/**
	 * Overriding the abstract class' private reference.
	 */
	private DocumentReference reference;

	public NodeClassInitializer() {
		// Since we can`t get the main wiki here, this is just to be able to use the Abstract class.
		super(Constants.SPACE, Node.CLASS+"Class");
	}

	@Override
	public boolean updateDocument(XWikiDocument document) {
		boolean needsUpdate = false;
		
		// Add missing class fields
		BaseClass baseClass = document.getXClass();
		needsUpdate |= baseClass.addTextField(FIELD_LABEL, FIELDPN_LABEL, 30);
		needsUpdate |= baseClass.addDBListField(FIELD_NODETYPE, FIELDPN_NODETYPE, "select distinct idprop.value from BaseObject as obj, StringProperty as idprop where obj.className='QuarXs.NodeTypeClass' and obj.id=idprop.id.id and idprop.id.name='prettyid'");
		needsUpdate |= baseClass.addTextField(FIELD_PRETTYID, FIELDPN_PRETTYID, 30);
		
		// Add missing document fields
		needsUpdate |= setClassDocumentFields(document, Node.CLASS+"Class");

		// Use Sheet to display documents having Class objects if no other class sheet is specified.
		if (this.classSheetBinder.getSheets(document).isEmpty()) {
			String wikiName = document.getDocumentReference().getWikiReference().getName();
			DocumentReference sheet = new DocumentReference(wikiName, Constants.SPACE, Node.CLASS+"Class"+"Sheet");
			needsUpdate |= this.classSheetBinder.bind(document, sheet);
		}

		return needsUpdate;
	}

	/**
	 * Initialize and return the main wiki's class document reference.
	 * 
	 * @return {@inheritDoc}
	 */
	@Override
	public EntityReference getDocumentReference() {
		if (this.reference == null) {
			synchronized (this) {
				if (this.reference == null) {
					String mainWikiName = xcontextProvider.get().getMainXWiki();
					this.reference = new DocumentReference(mainWikiName, Constants.SPACE, Node.CLASS+"Class");
				}
			}
		}

		return this.reference;
	}

}
