package de.dariah.schereg.util;

import java.util.ArrayList;
import java.util.Hashtable;

import de.dariah.base.model.base.SchemaElement;

public class SchemaElementContainer {

	private Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>> classLookupTable;
	private Hashtable<Integer, SchemaElement> idLookupTable;
	
	public Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>> getClassLookupTable() { return classLookupTable; }
	public void setClassLookupTable( Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>> classLookupTable) {
		this.classLookupTable = classLookupTable;
	}

	public Hashtable<Integer, SchemaElement> getIdLookupTable() { return idLookupTable; }
	public void setIdLookupTable(Hashtable<Integer, SchemaElement> idLookupTable) { 
		this.idLookupTable = idLookupTable;
	}

	public SchemaElementContainer() {
		classLookupTable = new Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>>();
		idLookupTable = new Hashtable<Integer, SchemaElement>();
	}

}
