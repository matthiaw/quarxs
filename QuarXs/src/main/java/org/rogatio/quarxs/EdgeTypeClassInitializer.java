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
@Named(EdgeType.CLASS + "Initializer")
@Singleton
public class EdgeTypeClassInitializer extends AbstractMandatoryDocumentInitializer {

	public static final String DEFAULT_FIELDS = "|";

	public static final String FIELD_NAME = "name";
	public static final String FIELDPN_NAME = "Name";
	
	public static final String FIELD_LABEL = "defaultlabel";
	public static final String FIELDPN_LABEL = "Default Label";

	public static final String FIELD_PRETTYID = "prettyid";
	public static final String FIELDPN_PRETTYID = "Humane Readable Id";
	
	public static final String FIELD_CONNECTION = "connection";
	public static final String FIELDPN_CONNECTION = "Connection";

	public static final String FIELD_WIDTH = "width";
    public static final String FIELDPN_WIDTH = "Width";
    
    public static final String FIELD_COLOR= "color";
    public static final String FIELDPN_COLOR = "Color";
    
    public static final String FIELD_ENTITY = "entity";
    public static final String FIELDPN_ENTITY = "Entity";
	
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

	public EdgeTypeClassInitializer() {
		// Since we can`t get the main wiki here, this is just to be able to use
		// the Abstract class.
		super(Constants.SPACE, EdgeType.CLASS + "Class");
	}

	@Override
	public boolean updateDocument(XWikiDocument document) {
		boolean needsUpdate = false;

		// Add missing class fields
		BaseClass baseClass = document.getXClass();

		needsUpdate |= baseClass.addTextField(FIELD_NAME, FIELDPN_NAME, 30);
		needsUpdate |= baseClass.addTextField(FIELD_LABEL, FIELDPN_LABEL, 30);
		needsUpdate |= baseClass.addTextField(FIELD_PRETTYID, FIELDPN_PRETTYID, 30);
		needsUpdate |= baseClass.addStaticListField(FIELD_CONNECTION, FIELDPN_CONNECTION, "Related|Unidirectional|Bidirectional");
		needsUpdate |= baseClass.addTextField(FIELD_WIDTH, FIELDPN_WIDTH, 30);
		needsUpdate |= baseClass.addTextField(FIELD_COLOR, FIELDPN_COLOR, 30);
		needsUpdate |= baseClass.addTextField(FIELD_ENTITY, FIELDPN_ENTITY, 30);
		
		// Add missing document fields
		needsUpdate |= setClassDocumentFields(document, EdgeType.CLASS + "Class");

		// Use Sheet to display documents having Class objects if no other class
		// sheet is specified.
		if (this.classSheetBinder.getSheets(document).isEmpty()) {
			String wikiName = document.getDocumentReference().getWikiReference().getName();
			DocumentReference sheet = new DocumentReference(wikiName, Constants.SPACE, EdgeType.CLASS + "Class" + "Sheet");
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
					this.reference = new DocumentReference(mainWikiName, Constants.SPACE, EdgeType.CLASS + "Class");
				}
			}
		}

		return this.reference;
	}

}
