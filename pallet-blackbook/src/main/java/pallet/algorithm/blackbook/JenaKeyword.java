package pallet.algorithm.blackbook;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import security.ejb.client.User;
import workflow.ejb.client.annotations.Execute;
import blackbook.algorithm.api.Algorithm;
import blackbook.algorithm.api.QueryRequest;
import blackbook.algorithm.api.URISetResponse;
import blackbook.algorithm.api.VoidParameter;
import blackbook.exception.BlackbookDataException;
import blackbook.exception.BlackbookSystemException;
import blackbook.jena.factory.JenaModelFactory;
import blackbook.jena.factory.JenaUtils;
import blackbook.metadata.manager.MetadataManagerFactory;
import blackbook.metadata.object.DataSourceMetadata;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This implements the Keyword interface for a Jena-based search.
 * 
 * In order to use JenaKeyword the developer must first have a database that
 * contains Jena RDF data. If one doesn't currently exist, the developer can
 * build this database using examples found in the config-test-data directory
 * structure in the delivered software.
 * 
 * Once a database is established, the JenaKeyword algorithm can then be
 * utilized in order to perform keyword look-ups against that database.
 * JenaKeyword will then return a list of URIs that contain the specified
 * keyword(s).
 */
public class JenaKeyword implements
        Algorithm<QueryRequest<VoidParameter>, URISetResponse> {
    /** Quote character. */
    private static final String QUOTE_CHAR = "\"";

    /** The AND operator. */
    private static final String AND = "AND";

    /** The OR operator. */
    private static final String OR = "OR";

    /** logger */
    private static Log logger = LogFactory.getLog(JenaKeyword.class);

    /**
     * This method returns a list of URIs that uniquely identify an RDF model
     * stored in Jena. The datasources are uniquely identified by a String with
     * a keyword as a String request. Multiple words in keyword assume an "OR"
     * in the request.
     * 
     * @param user
     *            the user making the request
     * @param request
     *            request object
     * @return algorithm response containing the new URIs
     * @throws BlackbookSystemException
     */
    @Execute
    public URISetResponse execute(User user, QueryRequest<VoidParameter> request)
            throws BlackbookSystemException {
        if (user == null) {
            throw new BlackbookSystemException("'user' cannot be null.");
        }

        if (request == null) {
            throw new BlackbookSystemException("'parameters' cannot be null.");
        }

        request.validate();

        Set<String> uris = keyword(request.getSourceDataSource(), request
                .getQuery(), user);

        // Prepare the destination data source and annotate the URIs...
        String destinationDataSource = JenaUtils.annotateSource(uris,
                getClass().getSimpleName(), request.getDestinationDataSource(),
                user);

        return new URISetResponse(uris, null, destinationDataSource);
    }

    /**
     * Performs the keyword search.
     * 
     * @param dataSource
     *            data source name
     * @param searchString
     *            keyword string
     * @param user
     *            user
     * @return list of URIs
     * @throws BlackbookSystemException
     */
    private Set<String> keyword(String dataSource, String searchString,
            User user) throws BlackbookSystemException {

        // if blank or null keyword, just return an empty set
        if (null == searchString || searchString.trim().length() == 0) {
            return new HashSet<String>();
        }

        DataSourceMetadata dataSourceMetadata = MetadataManagerFactory
                .getInstance().getDataSourceMetadataByName(dataSource);

        // If dataSourceMetadata is null, then the data source was not found.
        // Return an empty set of URIs...
        if (dataSourceMetadata == null) {
            return new HashSet<String>();
        }

        if (dataSourceMetadata.getKeywordAttributes() == null
                || dataSourceMetadata.getKeywordAttributes().isEmpty()) {
            throw new BlackbookSystemException(
                    "'keywordAttributes' unspecified for data source '"
                            + dataSourceMetadata.getName() + "'.");
        }

        // Return an empty list if the keyword list is empty...
        if (StringUtils.isBlank(searchString)) {
            return new HashSet<String>();
        }

        /*
         * At this point we have completed all of the initial error checking so
         * lets go!
         */

        // Get the model...
        Model model = JenaModelFactory.openModelByName(dataSource, user);

        QueryExecution qexec = null;
        Set<String> resultUris = new HashSet<String>();
        try {
            // Generate the query.
            String queryString = generateQuery(searchString, dataSourceMetadata);

            // Parse the query String into a query object.
            Query queryObj = QueryFactory.create(queryString);

            // Execute the query.
            qexec = QueryExecutionFactory.create(queryObj, model);
            ResultSet results = qexec.execSelect();

            // Collect the root URIs.
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                Resource subject = (Resource) qs.get("uri");
                JenaUtils.findRootURIs(subject, model, resultUris, null);
            }
            qexec.close();
        } catch (Exception e) {
            logger.error("Keyword failed.", e);
            throw new BlackbookSystemException("Keyword failed.", e);
        } finally {
            if (model != null) {
                model.close();
            }
        }

        return resultUris;
    }

    /**
     * Return the query generated from the searchString.
     * 
     * @param searchString
     *            the search string entered by the user
     * @param dataSourceMetadata
     *            the meta data for the data source
     * 
     * @return the query
     * 
     * @throws BlackbookDataException
     *             if unable to parse the searchString for a query
     */
    private String generateQuery(String searchString,
            DataSourceMetadata dataSourceMetadata)
            throws BlackbookDataException {
        /*
         * If there are no equals signs in the searchString, then do a simple
         * query.
         */
        if (searchString.indexOf("=") == -1) {
            /*
             * This is just a simple query to be performed against all
             * searchable fields.
             */
            return generateSimpleQuery(searchString, dataSourceMetadata);
        } else {
            /*
             * At this point we have an equals sign so generate the boolean
             * query.
             */
            return generateBooleanQuery(searchString, dataSourceMetadata);
        }
    }

    /**
     * Return the boolean query generated from the searchString.
     * 
     * @param searchString
     *            the search string entered by the user
     * @param dataSourceMetadata
     *            the meta data for the data source
     * 
     * @return the query
     * 
     * @throws BlackbookDataException
     *             if unable to parse the searchString for queries
     */
    private String generateBooleanQuery(String searchString,
            DataSourceMetadata dataSourceMetadata)
            throws BlackbookDataException {
        /*
         * First break the search up into a collection of queries. This is done
         * by splitting on the OR word in each search. The OR word corresponds
         * to the UNION of two queries.
         */
        String[] queriesToUnion = splitOnNonQuotedWord(searchString, OR);

        // Initialize the query
        StringBuffer queryBuff = new StringBuffer("SELECT ?uri WHERE { ");

        boolean atLeastOneValidEntry = false;
        /*
         * For each term we need to generate the SPARQL query segment, then
         * UNION them all together.
         */
        for (int i = 0; i < queriesToUnion.length; i++) {
            if (i > 0) {
                queryBuff.append(" UNION ");
            }

            String sparql = null;
            try {
                sparql = generateBooleanSegment(queriesToUnion[i]);
                queryBuff.append(" ");
                queryBuff.append(sparql);
                queryBuff.append(" ");
                atLeastOneValidEntry = true;
            } catch (BlackbookDataException e) {
                logger.error(e);
            }
        }

        // Add in the final closing items for this query.
        queryBuff.append(" } LIMIT ");
        queryBuff.append(dataSourceMetadata.getMaxUris());

        if (atLeastOneValidEntry) {
            return queryBuff.toString();
        } else {
            // no successful entries
            throw new BlackbookDataException("Unable to parse \""
                    + searchString + "\" for any query.");
        }
    }

    /**
     * Generate and return the sparql query from the queryString. The
     * queryString may contain logical ANDs, but no logical ORs. The queryString
     * must be like one of the following forms:
     * 
     * <pre>
     *     fieldName1=value1 AND fieldName2=&quot;value two&quot; 
     *     value1 &quot;value two&quot;
     * </pre>
     * 
     * or a combination of the previous 2 queries.
     * 
     * @param queryString
     * 
     * @return the sparql query
     * 
     * @throws BlackbookDataException
     *             if unable to parse the queryString
     */
    private String generateBooleanSegment(String queryString)
            throws BlackbookDataException {
        /*
         * If an AND is not encased by quote marks, it is a logical AND that we
         * use as a splitting point for the queries to be ANDed together. This
         * will return back an array of queries kind of like the following:
         * 
         * http://www.w3.org/2001/vcard-rdf/3.0#Family="Jones"
         * http://www.w3.org/2001/vcard-rdf/3.0#Given="Tom"
         * http://www.w3.org/2001/vcard-rdf/3.0#Other="And"
         * http://www.w3.org/2001/vcard-rdf/3.0#FN="Tom And Jones"
         * 
         * These need to be ANDed together in the sparql query by enclosing them
         * together in the same search wirh a period ('.') in beween them.
         */
        String[] andedQueries = splitOnNonQuotedWord(queryString, AND);

        // Initialize the sparql query segment.
        StringBuffer sparqlBuff = new StringBuffer("{ ");

        boolean atLeastOneEntry = false;

        for (int i = 0; i < andedQueries.length; i++) {
            /*
             * Now take the andedQueries that are like the following:
             * 
             * http://www.w3.org/2001/vcard-rdf/3.0#Family="Tom Jones"
             * 
             * and split on the EQUALS to get the 2 single elements.
             */
            String[] queryParts = andedQueries[i].split("=");

            if (queryParts.length == 2) {
                /*
                 * This is the case we are looking for, it is a single equals
                 * sign.
                 */
                sparqlBuff.append(generateInnerSegment(queryParts[0],
                        queryParts[1]));
                atLeastOneEntry = true;
            } else {
                /*
                 * If there are greater than 2 parts, then just continue to the
                 * next query.
                 */
                logger.error("Unable to parse \"" + andedQueries[i]
                        + "\" for a single equals sign");
            }

        }

        // Finalize the sparql query segment.
        sparqlBuff.append(" }");

        if (atLeastOneEntry) {
            return sparqlBuff.toString();
        } else {
            // no successful entries
            throw new BlackbookDataException("Unable to parse \"" + queryString
                    + "\" for any query with single equals signs.");
        }
    }

    /**
     * Generate a sparql query segment that is something like the following:
     * 
     * <pre>
     *     ?uri &lt;part1&gt; &quot;part2&quot; .
     * </pre>
     * 
     * NOTE: The actual values for part1 and part2 will be used in the segments.
     * 
     * @param part1
     *            first part
     * @param part2
     *            second part
     * 
     * @return the sparql query segment
     */
    private String generateInnerSegment(String part1, String part2) {
        /*
         * Generate something like the following:
         * 
         * ?uri <http://www.w3.org/2001/vcard-rdf/3.0#Given> "john smith" .
         */

        // Remove quotes from the query
        part2 = part2.replaceAll(QUOTE_CHAR, "");

        /*
         * Create the query form the parts. We will add quotes around the value
         * part of the query.
         */
        StringBuffer sparqlBuff = new StringBuffer(" ?uri <");
        sparqlBuff.append(part1);
        sparqlBuff.append("> \"");
        sparqlBuff.append(part2);
        sparqlBuff.append("\" . ");

        return sparqlBuff.toString();
    }

    /**
     * Split ,sort of like the String.split, but w/o regex and ignore anytime
     * the splitOn String is encased in QUOTE_CHAR. The resultant Strings will
     * all be trimmed of white space and no null or empty Strings will be
     * returned. This will only split on entire words and not on partial words.
     * That means that if you split on the word "leach" that you will not see a
     * split on the word "bleach".
     * 
     * NOTE: you cannot split on the QUOTE_CHAR.
     * 
     * NOTE: THis needs to be fixed to work on word boundaries.
     * 
     * @param searchString
     *            the String to split
     * @param splitOn
     *            the word to split on (not a regex).
     * 
     * @return the split array or null if failed
     */
    private String[] splitOnNonQuotedWord(String searchString, String splitOn) {
        if (searchString == null || splitOn == null
                || splitOn.equals(QUOTE_CHAR)) {
            // This returns an empty array since we cannot split.
            return new String[0];
        }

        /*
         * Set up the patterns so that the splitOn is surrounded by word
         * boundaries.
         */
        Pattern splitOnPattern = Pattern.compile("\\b" + splitOn + "\\b");
        Pattern quotePattern = Pattern.compile(QUOTE_CHAR);

        // Set up for the looping.
        ArrayList<String> splitStrings = new ArrayList<String>();
        int startIndex = 0;
        boolean moreQuotes = true;
        int nextSplitOnIndex = indexOf(splitOnPattern, searchString, 0);
        int openingQuoteIndex = indexOf(quotePattern, searchString, 0);
        int closingQuoteIndex = indexOf(quotePattern, searchString,
                openingQuoteIndex + 1);
        if (openingQuoteIndex == -1 || closingQuoteIndex == -1) {
            openingQuoteIndex = -1;
            closingQuoteIndex = -1;
            moreQuotes = false;
        }

        // Loop while there are more splitOn Strings in the searchString.
        while (nextSplitOnIndex != -1) {
            /*
             * Update the opening and closing quotes until the closingQuoteIndex
             * is past start of the splitOn String.
             */
            while (closingQuoteIndex <= nextSplitOnIndex && moreQuotes) {
                openingQuoteIndex = indexOf(quotePattern, searchString,
                        closingQuoteIndex + 1);
                closingQuoteIndex = indexOf(quotePattern, searchString,
                        openingQuoteIndex + 1);
                if (openingQuoteIndex == -1 || closingQuoteIndex == -1) {
                    openingQuoteIndex = -1;
                    closingQuoteIndex = -1;
                    moreQuotes = false;
                }
            }

            /*
             * Keep getting more splitOn Strings and use them to split, until we
             * get past the end of the closing quote. Ignore any splitOn String
             * that is between the opening and closing quotes.
             */
            while ((nextSplitOnIndex < closingQuoteIndex || !moreQuotes)
                    && nextSplitOnIndex != -1) {
                /*
                 * if the splitOn String is within quotes, then get the next one
                 * past the quotes.
                 */
                if (moreQuotes && (openingQuoteIndex < nextSplitOnIndex)
                        && (nextSplitOnIndex < closingQuoteIndex)
                        && (nextSplitOnIndex != -1)) {
                    // The splitOn String is within quotes, so get the next one.
                    nextSplitOnIndex = indexOf(splitOnPattern, searchString,
                            closingQuoteIndex + 1);
                } else {
                    /*
                     * Now we have found a string to split out, so add it to the
                     * list of splitStrings.
                     */
                    addIfNotEmpty(splitStrings, searchString.substring(
                            startIndex, nextSplitOnIndex));

                    // Set up for the next split.
                    startIndex = nextSplitOnIndex + splitOn.length();
                    nextSplitOnIndex = indexOf(splitOnPattern, searchString,
                            startIndex);
                }
            }
        }

        // Add in the final substring.
        addIfNotEmpty(splitStrings, searchString.substring(startIndex));

        // Convert to an array of Strings.
        String[] splitArray = splitStrings.toArray(new String[splitStrings
                .size()]);
        return splitArray;
    }

    /**
     * Looks up the location of a particular pattern in a string.
     * 
     * @param patternToMatch
     *            the pattern to look for
     * @param searchString
     *            the string being searched
     * @param startIndex
     *            the start index
     * @return the location of the pattern in the string, or -1
     */
    private int indexOf(Pattern patternToMatch, String searchString,
            int startIndex) {
        Matcher matcher = patternToMatch.matcher(searchString);

        if (matcher.find(startIndex)) {
            // Return the index
            return matcher.start();
        } else {
            // In this case the find failed
            return -1;
        }
    }

    /**
     * Add the stringToAdd to the splitStrings if it is not null or of zero
     * length.
     * 
     * @param splitStrings
     *            add to this list
     * @param stringToAdd
     *            the String to add
     */
    private void addIfNotEmpty(ArrayList<String> splitStrings,
            String stringToAdd) {
        if (stringToAdd == null) {
            return;
        }

        stringToAdd = stringToAdd.trim();

        if (stringToAdd.length() == 0) {
            return;
        }

        splitStrings.add(stringToAdd);
    }

    /**
     * Return the query generated from the keywords and all of the attributes.
     * 
     * @param searchString
     *            the search string entered by the user
     * 
     * @param dataSourceMetadata
     *            the meta data for the data source
     * 
     * @return the query
     */
    private String generateSimpleQuery(String searchString,
            DataSourceMetadata dataSourceMetadata) {
        // Get the list of keyword.
        List<String> keywordsList = parseSearchString(searchString);

        /*
         * The attributeUris is a list of the URIs from the owl file whose
         * values we search for the keywordsList elements.
         */
        List<String> attributeUris = dataSourceMetadata.getKeywordAttributes();

        // Initialize the query
        StringBuffer queryBuff = new StringBuffer("SELECT ?uri WHERE {");
        boolean firstQuery = true;
        for (String attrUri : attributeUris) {
            for (String keywordPhrase : keywordsList) {
                if (firstQuery) {
                    firstQuery = false;
                } else {
                    queryBuff.append("UNION");
                }
                queryBuff.append("{ ");
                queryBuff.append(generateInnerSegment(attrUri, keywordPhrase));
                queryBuff.append(" }");
            }
        }

        // Add in the final closing items for this query.
        queryBuff.append(" } LIMIT ");
        queryBuff.append(dataSourceMetadata.getMaxUris());

        return queryBuff.toString();
    }

    /**
     * Parse the searchString into the individual words or phrases to search
     * for. The phrases are a combination of words encased by the QUOTE_CHAR.
     * 
     * @param searchString
     *            the String to parse
     * 
     * @return the list of keyword strings
     */
    private List<String> parseSearchString(String searchString) {
        searchString = searchString.trim();

        ArrayList<String> phrasesList = new ArrayList<String>();
        int openingQuoteIndex = searchString.indexOf(QUOTE_CHAR);
        while (openingQuoteIndex != -1) {
            int closingQuoteIndex = searchString.indexOf(QUOTE_CHAR,
                    openingQuoteIndex + 1);
            if (closingQuoteIndex == -1) {
                /*
                 * Remove and ignore this final opening quote since it is not
                 * balanced.
                 */
                searchString = removeSubString(searchString, openingQuoteIndex,
                        openingQuoteIndex);
                break;
            }

            /*
             * Pull out the quoted phrase and add it to the list. I add 1 to the
             * openingQuoteIndex and I don't add 1 to the closingQuoteIndex so
             * that the phrase will not include the quote marks.
             */
            String phrase = searchString.substring(openingQuoteIndex + 1,
                    closingQuoteIndex);
            phrase = phrase.trim();
            if (phrase.length() > 0) {
                phrasesList.add(phrase);
            }

            /*
             * Remove the phrase (including the quote marks) from the
             * searchString.
             */
            searchString = removeSubString(searchString, openingQuoteIndex,
                    closingQuoteIndex);

            // Check for more opening quote marks.
            openingQuoteIndex = searchString.indexOf(QUOTE_CHAR);
        }

        /*
         * At this point we have extracted all of the quoted phrases and put
         * them into the list. Now just add in each of the remaining words by
         * splitting them on 1 or more white space characters.
         */
        searchString = searchString.trim();
        String[] remainingWords = searchString.split("\\s+");
        for (int i = 0; i < remainingWords.length; i++) {
            remainingWords[i] = remainingWords[i].trim();
            if (remainingWords[i].length() > 0) {
                phrasesList.add(remainingWords[i]);
            }
        }

        return phrasesList;
    }

    /**
     * Return the String with the substring starting at beginningIndex and
     * ending at endingIndex removed.
     * 
     * @param searchString
     *            the initial String
     * @param beginningIndex
     *            the beginning index of the substring to remove
     * @param endingIndex
     *            the ending index of the substring to remove
     * @return the String with the substring starting at beginningIndex and
     *         ending at endingIndex removed
     */
    private String removeSubString(String searchString, int beginningIndex,
            int endingIndex) {
        // Is the endingIndex the final character in the searchString?
        if (endingIndex == searchString.length() - 1) {
            // It is the final character, so just strip it off.
            searchString = searchString.substring(0, beginningIndex);
        } else {
            /*
             * The endingIndex is not the final character, so remove the
             * substring from the searchString.
             */
            searchString = searchString.substring(0, beginningIndex)
                    + searchString.substring(endingIndex + 1);
        }

        return searchString;
    }
}
