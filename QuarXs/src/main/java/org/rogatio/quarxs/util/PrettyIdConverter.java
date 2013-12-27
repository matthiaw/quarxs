package org.rogatio.quarxs.util;

import java.util.StringTokenizer;

/**
 * Parser for prettyId-String
 * 
 * @version $Id$
 */
public class PrettyIdConverter
{

    public static String getSpaceAndDocumentName(String prettyId) {
        return prettyId.substring(0, prettyId.lastIndexOf("."));
    }
    
    /**
     * Returns Space-Name
     * @param prettyId
     * @return
     */
    public static String getSpace(String prettyId)
    {

        if (prettyId == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(prettyId, ".");
        
        if (st.hasMoreTokens()) {
            return st.nextToken().trim();   
        }
        
        return null;
    }

    /**
     * Returns Document-Name
     * @param prettyId
     * @return
     */
    public static String getDocumentName(String prettyId)
    {

        if (prettyId == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(prettyId, ".");
        if (st.hasMoreTokens()) {
            st.nextToken();
        } else {
            return null;
        }
        if (st.hasMoreTokens()) {
            return st.nextToken().trim();
        } 
        return null;
    }

    /**
     * Returns LAbel of Node
     * @param prettyId
     * @return
     */
    public static String getName(String prettyId)
    {

        if (prettyId == null) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(prettyId, ".(");
        st.nextToken();
        st.nextToken();
        return st.nextToken().trim();
    }

    /**
     * Changes prettyId and replaces Label of Node
     * @param prettyid
     * @param name
     * @return
     */
    public static String replaceName(String prettyid, String name)
    {
        return getSpace(prettyid) + "." + getDocumentName(prettyid) + "." + name + " (" + getGuid(prettyid) + ")";
    }

    /**
     * Returns GUID of XObject Node or Edge
     * @param prettyId
     * @return
     */
    public static String getGuid(String prettyId)
    {
        try {
            return prettyId.substring(prettyId.indexOf("(") + 1, prettyId.length() - 1).trim();
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

}
