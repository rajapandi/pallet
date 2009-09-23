package utd.pallet.classification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import utd.pallet.classification.MalletTextDataTrainer.TrainerObject;
import utd.pallet.data.JenaModelFactory;
import utd.pallet.data.MalletAccuracyVector;
import utd.pallet.data.RDF2MalletInstances;
import utd.pallet.data.RDFUtils;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.types.InstanceList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.OWL;

public class BlackbookWorkflowSimulator {

    /**
     * log is created for the purpose of logging
     */
    private static Logger log = Logger
            .getLogger(BlackbookWorkflowSimulator.class);

    /**
     * charset encoding for getbytes
     */
    private static final String CHARSET = "UTF8";

    private static final String RDF_HEADER = "<?xml version=\"1.0\"?>\r\n"
            + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
            + "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\""
            + "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
            + "    xmlns:event=\"http://ebiquity.umbc.edu/ontology/event.owl#\""
            + "    xmlns:bb=\"http://blackbook.com/terms#\""
            + "    xmlns:geo=\"http://www.w3.org/2003/01/geo/wgs84_pos#\""
            + "    xmlns:vCard=\"http://www.w3.org/2001/vcard-rdf/3.0#\">";

    private static final String RDF_HOAX_WITH_LABEL = "<rdf:Description rdf:about=\"urn:monterey:incident1\">"
            + "        <dc:identifier>1</dc:identifier>"
            + "        <rdfs:label>incident</rdfs:label>"
            + "        <rdf:type rdf:resource = \"urn:monterey:incident\" />"
            + "        <event:startDate>1999-03-04</event:startDate>"
            + "        <vCard:ADR rdf:parseType=\"Resource\">"
            + "            <vCard:Locality>Lumberton</vCard:Locality>"
            + "            <vCard:Region>North Carolina</vCard:Region>"
            + "            <vCard:Country>United States</vCard:Country>"
            + "            <geo:Point rdf:parseType=\"Resource\">"
            + "                <geo:lat>34.618302</geo:lat>"
            + "                <geo:long>-79.012475</geo:long>"
            + "            </geo:Point>"
            + "        </vCard:ADR>"
            + "        <dc:source>[A] \"Lumberton Dialysis Clinic Targeted With Anthrax Scare,\" The News  Observer On The Web (5 March 1999); Internet, available at http://www.news-observer.com, accessed on 3/5/99.</dc:source>"
            + "        <bb:incidentDescription>An unidentified perpetrator called the Lumberton, North Carolina emergency communications center on 4 March 1999 and claimed that anthrax bacteria had been released in the Lumberton Dialysis Clinic. The city manager, Todd Powell, received the call. 40 people at the clinic were detained for three hours, most of whom were patients. Officials investigated the scene and instructed patients on self-decontamination procedures. According to FBI Spokeswoman Joanne Morley, a similar message was found written on a sheet of paper in the clinic's mailbox. Powell said at the time of the incident that officials had no evidence or indication that the claim was genuine.[A]</bb:incidentDescription>"
            + "        <bb:SUSPECTED>0</bb:SUSPECTED>"
            + "        <bb:LAST_MODIFIED>2005-08-08 11:03:29</bb:LAST_MODIFIED>"
            + "        <bb:STAT_TGT_GOVT>0</bb:STAT_TGT_GOVT>"
            + "        <bb:STAT_TGT_AGRO>0</bb:STAT_TGT_AGRO>"
            + "        <bb:MISC_INFO>No further information available.</bb:MISC_INFO>"
            + "        <bb:STAT_TGT_BUS>0</bb:STAT_TGT_BUS>"
            + "        <bb:G_SUSPECTED>0</bb:G_SUSPECTED>"
            + "        <bb:STAT_INCIDENT>Type 2: Criminally Motivated</bb:STAT_INCIDENT>"
            + "        <bb:STAT_TGT_ED>0</bb:STAT_TGT_ED>"
            + "        <bb:STAT_TGT_ORG>0</bb:STAT_TGT_ORG>"
            + "        <bb:STAT_TGT_UNK>0</bb:STAT_TGT_UNK>"
            + "        <bb:REGION>USA and Canada</bb:REGION>"
            + "        <bb:STAT_EVENT>Hoax/Prank</bb:STAT_EVENT>"
            + "        <bb:injuries>0</bb:injuries>"
            + "        <bb:STAT_TGT_INDISCRIM>0</bb:STAT_TGT_INDISCRIM>"
            + "        <bb:fatalities>0</bb:fatalities>"
            + "        <bb:STAT_TGT_MED>1</bb:STAT_TGT_MED>"
            + "        <bb:CREATED>2003-07-08 00:00:00</bb:CREATED>"
            + "        <bb:target>Dialysis clinic</bb:target>"
            + "        <bb:AGENT>bacillus anthracis</bb:AGENT>"
            + "        <bb:CBRN>biological</bb:CBRN>"
            + "        <bb:STAT_TGT_IND>0</bb:STAT_TGT_IND>"
            + "        <bb:STAT_TGT_NA>0</bb:STAT_TGT_NA>"
            + "    </rdf:Description>";

    private static final String RDF_HOAX_WITHOUT_LABEL = "<rdf:Description rdf:about=\"urn:monterey:incident1\">"
            + "        <dc:identifier>1</dc:identifier>"
            + "        <rdfs:label>incident</rdfs:label>"
            + "        <rdf:type rdf:resource = \"urn:monterey:incident\" />"
            + "        <event:startDate>1999-03-04</event:startDate>"
            + "        <vCard:ADR rdf:parseType=\"Resource\">"
            + "            <vCard:Locality>Lumberton</vCard:Locality>"
            + "            <vCard:Region>North Carolina</vCard:Region>"
            + "            <vCard:Country>United States</vCard:Country>"
            + "            <geo:Point rdf:parseType=\"Resource\">"
            + "                <geo:lat>34.618302</geo:lat>"
            + "                <geo:long>-79.012475</geo:long>"
            + "            </geo:Point>"
            + "        </vCard:ADR>"
            + "        <dc:source>[A] \"Lumberton Dialysis Clinic Targeted With Anthrax Scare,\" The News  Observer On The Web (5 March 1999); Internet, available at http://www.news-observer.com, accessed on 3/5/99.</dc:source>"
            + "        <bb:incidentDescription>An unidentified perpetrator called the Lumberton, North Carolina emergency communications center on 4 March 1999 and claimed that anthrax bacteria had been released in the Lumberton Dialysis Clinic. The city manager, Todd Powell, received the call. 40 people at the clinic were detained for three hours, most of whom were patients. Officials investigated the scene and instructed patients on self-decontamination procedures. According to FBI Spokeswoman Joanne Morley, a similar message was found written on a sheet of paper in the clinic's mailbox. Powell said at the time of the incident that officials had no evidence or indication that the claim was genuine.[A]</bb:incidentDescription>"
            + "        <bb:SUSPECTED>0</bb:SUSPECTED>"
            + "        <bb:LAST_MODIFIED>2005-08-08 11:03:29</bb:LAST_MODIFIED>"
            + "        <bb:STAT_TGT_GOVT>0</bb:STAT_TGT_GOVT>"
            + "        <bb:STAT_TGT_AGRO>0</bb:STAT_TGT_AGRO>"
            + "        <bb:MISC_INFO>No further information available.</bb:MISC_INFO>"
            + "        <bb:STAT_TGT_BUS>0</bb:STAT_TGT_BUS>"
            + "        <bb:G_SUSPECTED>0</bb:G_SUSPECTED>"
            + "        <bb:STAT_INCIDENT>Type 2: Criminally Motivated</bb:STAT_INCIDENT>"
            + "        <bb:STAT_TGT_ED>0</bb:STAT_TGT_ED>"
            + "        <bb:STAT_TGT_ORG>0</bb:STAT_TGT_ORG>"
            + "        <bb:STAT_TGT_UNK>0</bb:STAT_TGT_UNK>"
            + "        <bb:REGION>USA and Canada</bb:REGION>"
            + "        <bb:injuries>0</bb:injuries>"
            + "        <bb:STAT_TGT_INDISCRIM>0</bb:STAT_TGT_INDISCRIM>"
            + "        <bb:fatalities>0</bb:fatalities>"
            + "        <bb:STAT_TGT_MED>1</bb:STAT_TGT_MED>"
            + "        <bb:CREATED>2003-07-08 00:00:00</bb:CREATED>"
            + "        <bb:target>Dialysis clinic</bb:target>"
            + "        <bb:AGENT>bacillus anthracis</bb:AGENT>"
            + "        <bb:CBRN>biological</bb:CBRN>"
            + "        <bb:STAT_TGT_IND>0</bb:STAT_TGT_IND>"
            + "        <bb:STAT_TGT_NA>0</bb:STAT_TGT_NA>"
            + "    </rdf:Description>";

    private static final String RDF_THREAT_ONLY_WITH_LABEL = "<rdf:Description rdf:about=\"urn:monterey:incident66\">"
            + "<dc:identifier>66</dc:identifier>"
            + "<rdfs:label>incident</rdfs:label>"
            + "<rdf:type rdf:resource = \"urn:monterey:incident\" />"
            + "<event:startDate>1998-12-04</event:startDate>"
            + "<vCard:ADR rdf:parseType=\"Resource\">"
            + "    <vCard:Locality>Coppell</vCard:Locality>"
            + "    <vCard:Region>Texas</vCard:Region>"
            + "    <vCard:Country>United States</vCard:Country>"
            + "    <geo:Point rdf:parseType=\"Resource\">"
            + "        <geo:lat>32.976736</geo:lat>"
            + "        <geo:long>-96.990083</geo:long>"
            + "    </geo:Point>"
            + "</vCard:ADR>"
            + "<dc:source>[A] \"FBI Investigating Anthrax Scare at Suburban Post Office,\" Associate Press State  Local Wire (4 December 1998)."
            + "[B] Jason Sickles, \"FBI Doubts Vial in Coppell Mail Contains Anthrax,\" Dallas Morni</dc:source>"
            + "<bb:incidentDescription>At approximately 12:30 a.m. on 4 December 1998, at the bulk mail center of the United States Postal Service's Southwest Division, in Coppell, Texas, near the Dallas/Fort Worth Airport, a postal worker discovered a vial in an empty tray, with a handwritten message stating \"You have just been contaminated by anthrax\".[A,B,C,D] The postal worker discovered the unwrapped vial when she had gone to get a piece of equipment.[C,D] She picked up the vial, which may have contained a liquid, and carried it to the maintenance department after reporting the vial to her supervisor. The vial was then sealed in a hazardous materials container, and employees called 911.[C] Between 450 and 500 workers were at the center when the vial was found, but only four to six workers were exposed to the vial.[A,B,C,D] Investigators arrived at the postal center and detained the exposed workers until 3:45 a.m.[C] FBI investigators removed the four-inch vial and remained at the center until the scene was declared safe about five hours later.[A,B] The FBI later determined that the vial only contained water.[G]"
            + "On 6 January 2000, Steven Matthew Cutler, 27, was arrested in connection with the incident. Cutler, an emergency medical technician from Lewisville, Texas, a suburb of Dallas, was charged with threatening to use a weapon of mass destruction. He was indicted on two counts of threatening to use a weapon of mass destruction and  released on personal recognizance on 7 January 2000.[F] Cutler obtained the vials from his workplace, a medical laboratory in Irving, Texas.[G] Cutler is also charged with stealing a Social Security number from a patient at the laboratory and using it to apply for a credit card.] The indictment of Cutler was the first involving a case in which a perpetrator mailed an anthrax threat. Cutler faced up to 30 years in prison and a fine of $1 million if convicted.[G]"

            + "On 10 March 2000, Steven Cutler pleaded guilty to charges of making anthrax threats.[H] In July 2000, Cutler was sentenced to 21 months in federal prison and three years probation.[I]</bb:incidentDescription>"
            + "<bb:SUSPECTED>0</bb:SUSPECTED>"
            + "<bb:LAST_MODIFIED>2006-10-17 13:47:01</bb:LAST_MODIFIED>"
            + "<bb:STAT_TGT_GOVT>1</bb:STAT_TGT_GOVT>"
            + "<bb:STAT_TGT_AGRO>0</bb:STAT_TGT_AGRO>"
            + "<bb:SPEC_MOTIVE>unknown, most likely a prank</bb:SPEC_MOTIVE>"
            + "<bb:STAT_DELIVERY>Jug/Jar/Canister</bb:STAT_DELIVERY>"
            + "<bb:MISC_INFO>no further information available</bb:MISC_INFO>"
            + "<bb:STAT_TGT_BUS>0</bb:STAT_TGT_BUS>"
            + "<bb:G_SUSPECTED>0</bb:G_SUSPECTED>"
            + "<bb:STAT_INCIDENT>Type 2: Criminally Motivated</bb:STAT_INCIDENT>"
            + "<bb:STAT_TGT_ED>0</bb:STAT_TGT_ED>"
            + "<bb:STAT_TGT_ORG>0</bb:STAT_TGT_ORG>"
            + "<bb:STAT_TGT_UNK>0</bb:STAT_TGT_UNK>"
            + "<bb:REGION>USA and Canada</bb:REGION>"
            + "<bb:STAT_EVENT>Threat Only</bb:STAT_EVENT>"
            + "<bb:injuries>0</bb:injuries>"
            + "<bb:STAT_TGT_INDISCRIM>1</bb:STAT_TGT_INDISCRIM>"
            + "<bb:fatalities>0</bb:fatalities>"
            + "<bb:STAT_TGT_MED>0</bb:STAT_TGT_MED>"
            + "<bb:CREATED>2004-12-08 00:00:00</bb:CREATED>"
            + "<bb:ACTOR>Steven Matthew Cutler</bb:ACTOR>"
            + "<bb:target>employees of United States Postal Service Division</bb:target>"
            + "<bb:AGENT>bacillus anthracis</bb:AGENT>"
            + "<bb:CBRN>biological</bb:CBRN>"
            + "<bb:STAT_TGT_IND>0</bb:STAT_TGT_IND>"
            + "<bb:STAT_TGT_NA>0</bb:STAT_TGT_NA>"
            + "<bb:SP_DELIVERY>vial of liquid sent through the mail</bb:SP_DELIVERY>"
            + "</rdf:Description>";

    private static final String RDF_THREAT_ONLY_WITHOUT_LABEL = "<rdf:Description rdf:about=\"urn:monterey:incident66\">"
            + "<dc:identifier>66</dc:identifier>"
            + "<rdfs:label>incident</rdfs:label>"
            + "<rdf:type rdf:resource = \"urn:monterey:incident\" />"
            + "<event:startDate>1998-12-04</event:startDate>"
            + "<vCard:ADR rdf:parseType=\"Resource\">"
            + "    <vCard:Locality>Coppell</vCard:Locality>"
            + "    <vCard:Region>Texas</vCard:Region>"
            + "    <vCard:Country>United States</vCard:Country>"
            + "    <geo:Point rdf:parseType=\"Resource\">"
            + "        <geo:lat>32.976736</geo:lat>"
            + "        <geo:long>-96.990083</geo:long>"
            + "    </geo:Point>"
            + "</vCard:ADR>"
            + "<dc:source>[A] \"FBI Investigating Anthrax Scare at Suburban Post Office,\" Associate Press State  Local Wire (4 December 1998)."
            + "[B] Jason Sickles, \"FBI Doubts Vial in Coppell Mail Contains Anthrax,\" Dallas Morni</dc:source>"
            + "<bb:incidentDescription>At approximately 12:30 a.m. on 4 December 1998, at the bulk mail center of the United States Postal Service's Southwest Division, in Coppell, Texas, near the Dallas/Fort Worth Airport, a postal worker discovered a vial in an empty tray, with a handwritten message stating \"You have just been contaminated by anthrax\".[A,B,C,D] The postal worker discovered the unwrapped vial when she had gone to get a piece of equipment.[C,D] She picked up the vial, which may have contained a liquid, and carried it to the maintenance department after reporting the vial to her supervisor. The vial was then sealed in a hazardous materials container, and employees called 911.[C] Between 450 and 500 workers were at the center when the vial was found, but only four to six workers were exposed to the vial.[A,B,C,D] Investigators arrived at the postal center and detained the exposed workers until 3:45 a.m.[C] FBI investigators removed the four-inch vial and remained at the center until the scene was declared safe about five hours later.[A,B] The FBI later determined that the vial only contained water.[G]"
            + "On 6 January 2000, Steven Matthew Cutler, 27, was arrested in connection with the incident. Cutler, an emergency medical technician from Lewisville, Texas, a suburb of Dallas, was charged with threatening to use a weapon of mass destruction. He was indicted on two counts of threatening to use a weapon of mass destruction and  released on personal recognizance on 7 January 2000.[F] Cutler obtained the vials from his workplace, a medical laboratory in Irving, Texas.[G] Cutler is also charged with stealing a Social Security number from a patient at the laboratory and using it to apply for a credit card.] The indictment of Cutler was the first involving a case in which a perpetrator mailed an anthrax threat. Cutler faced up to 30 years in prison and a fine of $1 million if convicted.[G]"

            + "On 10 March 2000, Steven Cutler pleaded guilty to charges of making anthrax threats.[H] In July 2000, Cutler was sentenced to 21 months in federal prison and three years probation.[I]</bb:incidentDescription>"
            + "<bb:SUSPECTED>0</bb:SUSPECTED>"
            + "<bb:LAST_MODIFIED>2006-10-17 13:47:01</bb:LAST_MODIFIED>"
            + "<bb:STAT_TGT_GOVT>1</bb:STAT_TGT_GOVT>"
            + "<bb:STAT_TGT_AGRO>0</bb:STAT_TGT_AGRO>"
            + "<bb:SPEC_MOTIVE>unknown, most likely a prank</bb:SPEC_MOTIVE>"
            + "<bb:STAT_DELIVERY>Jug/Jar/Canister</bb:STAT_DELIVERY>"
            + "<bb:MISC_INFO>no further information available</bb:MISC_INFO>"
            + "<bb:STAT_TGT_BUS>0</bb:STAT_TGT_BUS>"
            + "<bb:G_SUSPECTED>0</bb:G_SUSPECTED>"
            + "<bb:STAT_INCIDENT>Type 2: Criminally Motivated</bb:STAT_INCIDENT>"
            + "<bb:STAT_TGT_ED>0</bb:STAT_TGT_ED>"
            + "<bb:STAT_TGT_ORG>0</bb:STAT_TGT_ORG>"
            + "<bb:STAT_TGT_UNK>0</bb:STAT_TGT_UNK>"
            + "<bb:REGION>USA and Canada</bb:REGION>"
            + "<bb:injuries>0</bb:injuries>"
            + "<bb:STAT_TGT_INDISCRIM>1</bb:STAT_TGT_INDISCRIM>"
            + "<bb:fatalities>0</bb:fatalities>"
            + "<bb:STAT_TGT_MED>0</bb:STAT_TGT_MED>"
            + "<bb:CREATED>2004-12-08 00:00:00</bb:CREATED>"
            + "<bb:ACTOR>Steven Matthew Cutler</bb:ACTOR>"
            + "<bb:target>employees of United States Postal Service Division</bb:target>"
            + "<bb:AGENT>bacillus anthracis</bb:AGENT>"
            + "<bb:CBRN>biological</bb:CBRN>"
            + "<bb:STAT_TGT_IND>0</bb:STAT_TGT_IND>"
            + "<bb:STAT_TGT_NA>0</bb:STAT_TGT_NA>"
            + "<bb:SP_DELIVERY>vial of liquid sent through the mail</bb:SP_DELIVERY>"
            + "</rdf:Description>";

    private static final String RDF_USE_OF_AGENT_WITH_LABEL = "<rdf:Description rdf:about=\"urn:monterey:incident69\">"
            + "<dc:identifier>69</dc:identifier>"
            + "<rdfs:label>incident</rdfs:label>"
            + "<rdf:type rdf:resource = \"urn:monterey:incident\" />"
            + "<event:startDate>1998-12-29</event:startDate>"

            + "<vCard:ADR rdf:parseType=\"Resource\">"
            + "<vCard:Locality>Argun, Chechnya</vCard:Locality>"

            + "<vCard:Country>Russian Federation</vCard:Country>"
            + "<geo:Point rdf:parseType=\"Resource\">"
            + "    <geo:lat>43.292511</geo:lat>"
            + "    <geo:long>45.867081</geo:long>"
            + "</geo:Point>"
            + "</vCard:ADR>"
            + "<dc:source>[A] Mined Radioactive Container Found in Chechnya,Agence France Presse (29 December 1998): 08:31 GMT."
            + "[B] Mined Radioactive Container Found in Chechnya: security official, Agence France Presse (</dc:source>"
            + "<bb:incidentDescription>On 29 December 1998, a booby-trapped or mined container of radioactive material emitting a highly radioactive substance was discovered near a railway track in Argun, Chechnya.[A,B] The United Press International (UPI) stated that sources from Argun report the radiation level is huge and poses a hazard to human health.[H] Argun is located approximately 9 miles due east of the Chechen capital of Grozny.[A,B]"
            + "The announcement was issued by Ibragim Khultygov, director of security services, over Chechen television.[A] Khultygov did not reveal the location in which the container was being deactivated.[A] Reports from Agence France Press that cited ITAR-TASS news agency, indicated that specialists cordoned off the area and worked for several hours on dismantling the explosive device at the scene.[C,D,F] Specialists from the Ministry of Emergencies managed to deactivate the device and remove the container to an undisclosed location without incident. By 31 December 1998, a report from Radiostantsiya Ekho Moskvy suggested that we will probably be right to assume that this was not just an accident but a planned act of sabotage.[J]"

            + "In January 1998, containers of radioactive material were found near this location;[H,I] however, it is only in the same UPI article mentioned above that this container was described as de-activated.[H] Press reports issued at the time of the incident do not indicate the presence of any type of explosive device. (See Miscellaneous information for more about the January 1998 container found near Argun.)</bb:incidentDescription>"
            + "<bb:SUSPECTED>0</bb:SUSPECTED>"
            + "<bb:LAST_MODIFIED>2006-10-17 13:47:01</bb:LAST_MODIFIED>"
            + "<bb:STAT_TGT_GOVT>0</bb:STAT_TGT_GOVT>"
            + "<bb:STAT_TGT_AGRO>0</bb:STAT_TGT_AGRO>"
            + "<bb:STAT_DELIVERY>Explosive Device</bb:STAT_DELIVERY>"
            + "<bb:MISC_INFO>After a mined container of radioactive material was found in Argun, Chechnya, press reports referenced the discovery of another radioactive container one year earlier. The reference stated that the</bb:MISC_INFO>"
            + "<bb:STAT_TGT_BUS>0</bb:STAT_TGT_BUS>"
            + "<bb:G_SUSPECTED>0</bb:G_SUSPECTED>"
            + "<bb:STAT_INCIDENT>Type 1: Politically / Ideologically Motivated</bb:STAT_INCIDENT>"
            + "<bb:STAT_TGT_ED>0</bb:STAT_TGT_ED>"
            + "<bb:STAT_TGT_ORG>0</bb:STAT_TGT_ORG>"
            + "<bb:STAT_TGT_UNK>1</bb:STAT_TGT_UNK>"
            + "<bb:REGION>Russia and NIS</bb:REGION>"
            + "<bb:STAT_EVENT>Use of Agent</bb:STAT_EVENT>"
            + "<bb:injuries>0</bb:injuries>"
            + "<bb:STAT_TGT_INDISCRIM>0</bb:STAT_TGT_INDISCRIM>"
            + "<bb:fatalities>0</bb:fatalities>"
            + "<bb:STAT_TGT_MED>0</bb:STAT_TGT_MED>"
            + "<bb:CREATED>2003-03-07 00:00:00</bb:CREATED>"
            + "<bb:AGENT>unknown radiological material</bb:AGENT>"
            + "<bb:CBRN>radiological</bb:CBRN>"
            + "<bb:STAT_TGT_IND>0</bb:STAT_TGT_IND>"
            + "<bb:STAT_TGT_NA>0</bb:STAT_TGT_NA>" + "</rdf:Description>";

    private static final String RDF_USE_OF_AGENT_WITHOUT_LABEL = "<rdf:Description rdf:about=\"urn:monterey:incident69\">"
            + "<dc:identifier>69</dc:identifier>"
            + "<rdfs:label>incident</rdfs:label>"
            + "<rdf:type rdf:resource = \"urn:monterey:incident\" />"
            + "<event:startDate>1998-12-29</event:startDate>"
            + "<vCard:ADR rdf:parseType=\"Resource\">"
            + "<vCard:Locality>Argun, Chechnya</vCard:Locality>"
            + "<vCard:Country>Russian Federation</vCard:Country>"
            + "<geo:Point rdf:parseType=\"Resource\">"
            + "    <geo:lat>43.292511</geo:lat>"
            + "    <geo:long>45.867081</geo:long>"
            + "</geo:Point>"
            + "</vCard:ADR>"
            + "<dc:source>[A] Mined Radioactive Container Found in Chechnya, Agence France Presse (29 December 1998): 08:31 GMT."
            + "[B] Mined Radioactive Container Found in Chechnya: security official, Agence France Presse (</dc:source>"
            + "<bb:incidentDescription>On 29 December 1998, a booby-trapped or mined container of radioactive material emitting a highly radioactive substance was discovered near a railway track in Argun, Chechnya.[A,B] The United Press International (UPI) stated that sources from Argun report the radiation level is huge and poses a hazard to human health.[H] Argun is located approximately 9 miles due east of the Chechen capital of Grozny.[A,B]"
            + "The announcement was issued by Ibragim Khultygov, director of security services, over Chechen television.[A] Khultygov did not reveal the location in which the container was being deactivated.[A] Reports from Agence France Press that cited ITAR-TASS news agency, indicated that specialists cordoned off the area and worked for several hours on dismantling the explosive device at the scene.[C,D,F] Specialists from the Ministry of Emergencies managed to deactivate the device and remove the container to an undisclosed location without incident. By 31 December 1998, a report from Radiostantsiya Ekho Moskvy suggested that we will probably be right to assume that this was not just an accident but a planned act of sabotage.[J]"
            + "In January 1998, containers of radioactive material were found near this location;[H,I] however, it is only in the same UPI article mentioned above that this container was described as de-activated.[H] Press reports issued at the time of the incident do not indicate the presence of any type of explosive device. (See Miscellaneous information for more about the January 1998 container found near Argun.)</bb:incidentDescription>"
            + "<bb:SUSPECTED>0</bb:SUSPECTED>"
            + "<bb:LAST_MODIFIED>2006-10-17 13:47:01</bb:LAST_MODIFIED>"
            + "<bb:STAT_TGT_GOVT>0</bb:STAT_TGT_GOVT>"
            + "<bb:STAT_TGT_AGRO>0</bb:STAT_TGT_AGRO>"
            + "<bb:STAT_DELIVERY>Explosive Device</bb:STAT_DELIVERY>"
            + "<bb:MISC_INFO>After a mined container of radioactive material was found in Argun, Chechnya, press reports referenced the discovery of another radioactive container one year earlier. The reference stated that the</bb:MISC_INFO>"
            + "<bb:STAT_TGT_BUS>0</bb:STAT_TGT_BUS>"
            + "<bb:G_SUSPECTED>0</bb:G_SUSPECTED>"
            + "<bb:STAT_INCIDENT>Type 1: Politically / Ideologically Motivated</bb:STAT_INCIDENT>"
            + "<bb:STAT_TGT_ED>0</bb:STAT_TGT_ED>"
            + "<bb:STAT_TGT_ORG>0</bb:STAT_TGT_ORG>"
            + "<bb:STAT_TGT_UNK>1</bb:STAT_TGT_UNK>"
            + "<bb:REGION>Russia and NIS</bb:REGION>"
            + "<bb:injuries>0</bb:injuries>"
            + "<bb:STAT_TGT_INDISCRIM>0</bb:STAT_TGT_INDISCRIM>"
            + "<bb:fatalities>0</bb:fatalities>"
            + "<bb:STAT_TGT_MED>0</bb:STAT_TGT_MED>"
            + "<bb:CREATED>2003-03-07 00:00:00</bb:CREATED>"
            + "<bb:AGENT>unknown radiological material</bb:AGENT>"
            + "<bb:CBRN>radiological</bb:CBRN>"
            + "<bb:STAT_TGT_IND>0</bb:STAT_TGT_IND>"
            + "<bb:STAT_TGT_NA>0</bb:STAT_TGT_NA>" + "</rdf:Description>";

    private static final String RDF_PLOT_ONLY_WITH_LABEL = "<rdf:Description rdf:about=\"urn:monterey:incident147\">"
            + "<dc:identifier>147</dc:identifier>"
            + "<rdfs:label>incident</rdfs:label>"
            + "<rdf:type rdf:resource = \"urn:monterey:incident\" />"
            + "<event:startDate>1998-04-00</event:startDate>"
            + "<vCard:ORG rdf:resource=\"urn:monterey:organization147\" />"
            + "<vCard:ADR rdf:parseType=\"Resource\">"
            + "<vCard:Locality>Amman</vCard:Locality>"
            + "<vCard:Country>Jordan</vCard:Country>"
            + "<geo:Point rdf:parseType=\"Resource\">"
            + "<geo:lat>31.949841</geo:lat>"
            + "<geo:long>35.922371</geo:long>"
            + "</geo:Point>"
            + "</vCard:ADR>"
            + "<dc:source>[A] Quote-Unquote, The Sleeping Giant (date not given); Internet, available from http://www.geocities.com/CapitolHill/Congress/7663/Quote.html, accessed on 6/28/99."
            + "[B] Nirenstein Fiamma, E Arafat</dc:source>"
            + "<bb:incidentDescription>In April 1998, Palestinian Islamic Jihad leader Nassar Asad Al-Tamimi discussed the possibility of the acquisition of biological weapons by his organization.  He was speaking at a Hamas memorial service in Amman, Jordan.  According to a report by the Center for Israeli Civilian Empowerment, Al Tamimi stated, Jihad has at last discovered how to win the holy war -- lethal germs.[A,B,C,D]</bb:incidentDescription>"
            + "<bb:SUSPECTED>0</bb:SUSPECTED>"
            + "<bb:STAT_MOTIVE>To Act Because of an Ideology/Belief System</bb:STAT_MOTIVE>"
            + "<bb:LAST_MODIFIED>2006-10-17 13:47:01</bb:LAST_MODIFIED>"
            + "<bb:STAT_TGT_GOVT>0</bb:STAT_TGT_GOVT>"
            + "<bb:STAT_TGT_AGRO>0</bb:STAT_TGT_AGRO>"
            + "<bb:SPEC_MOTIVE>To further group objectives, part of an overall campaign against non-Muslims</bb:SPEC_MOTIVE>"
            + "<bb:MISC_INFO>No further information available</bb:MISC_INFO>"
            + "<bb:STAT_TGT_BUS>0</bb:STAT_TGT_BUS>"
            + "<bb:G_SUSPECTED>0</bb:G_SUSPECTED>"
            + "<bb:STAT_INCIDENT>Type 1: Politically / Ideologically Motivated</bb:STAT_INCIDENT>"
            + "<bb:STAT_TGT_ED>0</bb:STAT_TGT_ED>"
            + "<bb:STAT_TGT_ORG>0</bb:STAT_TGT_ORG>"
            + "<bb:STAT_TGT_UNK>1</bb:STAT_TGT_UNK>"
            + "<bb:REGION>Middle East and North Africa</bb:REGION>"
            + "<bb:STAT_EVENT>Plot Only</bb:STAT_EVENT>"
            + "<bb:injuries>0</bb:injuries>"
            + "<bb:STAT_TGT_INDISCRIM>0</bb:STAT_TGT_INDISCRIM>"
            + "<bb:fatalities>0</bb:fatalities>"
            + "<bb:STAT_TGT_MED>0</bb:STAT_TGT_MED>"
            + "<bb:CREATED>2006-10-17 13:47:01</bb:CREATED>"
            + "<bb:ACTOR>Asad Al-Tamimi</bb:ACTOR>"
            + "<bb:AGENT>unknown biological agent</bb:AGENT>"
            + "<bb:CBRN>biological</bb:CBRN>"
            + "<bb:STAT_TGT_IND>0</bb:STAT_TGT_IND>"
            + "<bb:STAT_TGT_NA>0</bb:STAT_TGT_NA>" + "</rdf:Description>";

    private static final String RDF_PLOT_ONLY_WITHOUT_LABEL = "<rdf:Description rdf:about=\"urn:monterey:incident147\">"
            + "<dc:identifier>147</dc:identifier>"
            + "<rdfs:label>incident</rdfs:label>"
            + "<rdf:type rdf:resource = \"urn:monterey:incident\" />"
            + "<event:startDate>1998-04-00</event:startDate>"
            + "<vCard:ORG rdf:resource=\"urn:monterey:organization147\" />"
            + "<vCard:ADR rdf:parseType=\"Resource\">"
            + "<vCard:Locality>Amman</vCard:Locality>"
            + "<vCard:Country>Jordan</vCard:Country>"
            + "<geo:Point rdf:parseType=\"Resource\">"
            + "<geo:lat>31.949841</geo:lat>"
            + "<geo:long>35.922371</geo:long>"
            + "</geo:Point>"
            + "</vCard:ADR>"
            + "<dc:source>[A] Quote-Unquote, The Sleeping Giant (date not given); Internet, available from http://www.geocities.com/CapitolHill/Congress/7663/Quote.html, accessed on 6/28/99."
            + "[B] Nirenstein Fiamma, E Arafat</dc:source>"
            + "<bb:incidentDescription>In April 1998, Palestinian Islamic Jihad leader Nassar Asad Al-Tamimi discussed the possibility of the acquisition of biological weapons by his organization.  He was speaking at a Hamas memorial service in Amman, Jordan.  According to a report by the Center for Israeli Civilian Empowerment, Al Tamimi stated, Jihad has at last discovered how to win the holy war -- lethal germs.[A,B,C,D]</bb:incidentDescription>"
            + "<bb:SUSPECTED>0</bb:SUSPECTED>"
            + "<bb:STAT_MOTIVE>To Act Because of an Ideology/Belief System</bb:STAT_MOTIVE>"
            + "<bb:LAST_MODIFIED>2006-10-17 13:47:01</bb:LAST_MODIFIED>"
            + "<bb:STAT_TGT_GOVT>0</bb:STAT_TGT_GOVT>"
            + "<bb:STAT_TGT_AGRO>0</bb:STAT_TGT_AGRO>"
            + "<bb:SPEC_MOTIVE>To further group objectives, part of an overall campaign against non-Muslims</bb:SPEC_MOTIVE>"
            + "<bb:MISC_INFO>No further information available</bb:MISC_INFO>"
            + "<bb:STAT_TGT_BUS>0</bb:STAT_TGT_BUS>"
            + "<bb:G_SUSPECTED>0</bb:G_SUSPECTED>"
            + "<bb:STAT_INCIDENT>Type 1: Politically / Ideologically Motivated</bb:STAT_INCIDENT>"
            + "<bb:STAT_TGT_ED>0</bb:STAT_TGT_ED>"
            + "<bb:STAT_TGT_ORG>0</bb:STAT_TGT_ORG>"
            + "<bb:STAT_TGT_UNK>1</bb:STAT_TGT_UNK>"
            + "<bb:REGION>Middle East and North Africa</bb:REGION>"
            + "<bb:injuries>0</bb:injuries>"
            + "<bb:STAT_TGT_INDISCRIM>0</bb:STAT_TGT_INDISCRIM>"
            + "<bb:fatalities>0</bb:fatalities>"
            + "<bb:STAT_TGT_MED>0</bb:STAT_TGT_MED>"
            + "<bb:CREATED>2006-10-17 13:47:01</bb:CREATED>"
            + "<bb:ACTOR>Asad Al-Tamimi</bb:ACTOR>"
            + "<bb:AGENT>unknown biological agent</bb:AGENT>"
            + "<bb:CBRN>biological</bb:CBRN>"
            + "<bb:STAT_TGT_IND>0</bb:STAT_TGT_IND>"
            + "<bb:STAT_TGT_NA>0</bb:STAT_TGT_NA>" + "</rdf:Description>";

    private static final String RDF_FOOTER = "</rdf:RDF>";

    private static final Resource HOAX_RESOURCE = ModelFactory
            .createDefaultModel().createResource("urn:monterey:incident1");

    private static final Resource THREAT_RESOURCE = ModelFactory
            .createDefaultModel().createResource("urn:monterey:incident66");

    private static final Resource USE_RESOURCE = ModelFactory
            .createDefaultModel().createResource("urn:monterey:incident69");

    private static final Resource PLOT_RESOURCE = ModelFactory
            .createDefaultModel().createResource("urn:monterey:incident147");
    private static final Property BEST_LABEL = ModelFactory
            .createDefaultModel()
            .createProperty(
                    "http://marathonminds.com//MalletClassification//#hasBestLabel");

    private static final Property HAS_VALUE = ModelFactory
            .createDefaultModel()
            .createProperty(
                    "http://marathonminds.com//MalletClassification//#hasValue");
    // a list of keywords that should be classified a hoax
    private static final String HOAX_KEYWORDS = "";

    // a list of keywords that should be classified a threat only
    private static final String THEAT_KEYWORDS = "";

    // a list of keywords that should be classified a use of agent
    private static final String USE_KEYWORDS = "";

    // a list of keywords that should be classified a plot only
    private static final String PLOT_KEYWORDS = "";

    private static final Property CLASSIFICATION_PROPERTY = ModelFactory
            .createDefaultModel().createProperty(
                    "http://blackbook.com/terms#STAT_EVENT");

    private static final String HOAX_LABEL = "Hoax/Prank";

    private static final String THREAT_ONLY_LABEL = "Threat Only";

    private static final String PLOT_ONLY_LABEL = "Plot Only";

    private static final String USE_OF_AGENT_LABEL = "Use of Agent";

    private static final String ATTEMPTED_ACCQUISTION_LABEL = "Attempted Acquisition";

    private static final String THREAT_WITH_POSSESSION_LABEL = "Threat with Possession";

    private static final String POSSESSION_ONLY_LABEL = "Possession Only";

    private static final String FALSE_CASE_LABEL = "False Case";

    private static final Resource RESOURCE_HOAX = ModelFactory
            .createDefaultModel()
            .createResource("http://marathonminds//event1");

    private static final Resource RESOURCE_PLOT_ONLY = ModelFactory
            .createDefaultModel()
            .createResource("http://marathonminds//event2");

    private static final Resource RESOURCE_USE_OF_AGENT = ModelFactory
            .createDefaultModel()
            .createResource("http://marathonminds//event3");

    private static final Resource RESOURCE_ATTEMPTED_ACCQUISTION = ModelFactory
            .createDefaultModel()
            .createResource("http://marathonminds//event4");

    private static final Resource RESOURCE_THREAT_ONLY = ModelFactory
            .createDefaultModel()
            .createResource("http://marathonminds//event5");

    private static final Resource RESOURCE_THREAT_WITH_POSSESSION = ModelFactory
            .createDefaultModel()
            .createResource("http://marathonminds//event6");

    private static final Resource RESOURCE_POSSESSION_ONLY = ModelFactory
            .createDefaultModel()
            .createResource("http://marathonminds//event7");

    private static final Resource RESOURCE_FALSE_CASE = ModelFactory
            .createDefaultModel()
            .createResource("http://marathonminds//event8");

    private static final Resource RESOURCE_PLOT_ONLY_2 = ModelFactory
            .createDefaultModel()
            .createResource("http://marathonminds//event9");

    private static final Resource RESOURCE_PLOT_ONLY_3 = ModelFactory
            .createDefaultModel().createResource(
                    "http://marathonminds//event10");

    private static final Resource RESOURCE_HOAX_2 = ModelFactory
            .createDefaultModel().createResource(
                    "http://marathonminds//event11");

    /**
     * @param args
     *            Command line arguments (first arg is the file name to
     *            process).
     * @throws Exception
     */
    public static String generateTestModel() {
        Model model = ModelFactory.createDefaultModel();
        String str = null;
        Resource resourceHoax = model
                .createResource("http://marathonminds//event1");
        Resource resourcePlotOnly = model
                .createResource("http://marathonminds//event2");
        Resource resourceUseOfAgent = model
                .createResource("http://marathonminds//event3");
        Resource resourceAttemptedAccquistion = model
                .createResource("http://marathonminds//event4");
        Resource resourceThreatOnly = model
                .createResource("http://marathonminds//event5");
        Resource resourceThreatWithPossession = model
                .createResource("http://marathonminds//event6");
        Resource resourcePossessionOnly = model
                .createResource("http://marathonminds//event7");
        Resource resourceFalseCase = model
                .createResource("http://marathonminds//event8");
        Resource resourcePlotOnly2 = model
                .createResource("http://marathonminds//event9");
        Resource resourcePlotOnly3 = model
                .createResource("http://marathonminds//event10");
        Resource resourceHoax2 = model
                .createResource("http://marathonminds//event11");
        /*
         * Resource r1 is describing an incident which is of type of Hoax/Prank
         */

        Property p1 = model
                .createProperty("http://marathonminds//event1#description");
        resourceHoax
                .addProperty(
                        p1,
                        "On Saturday, Police in Northampton spent many hours trying to find a woman who called 999 to claim she was being held against her will.Northamptonshire Police investigated the woman and found no evidence of the crime.Later on it was established that she had made a prank call");
        /*
         * Resource r2 is describing an incident which is of type of Plot Only
         */

        Property p2 = model
                .createProperty("http://marathonminds//event2#description");
        resourcePlotOnly
                .addProperty(
                        p2,
                        "Security at the famous Akshardham temple in the national capital was beefed up today after police received intelligence inputs that Kashmiri militants planned to blow it up.We received an input that some militants belonging to Kashmir-based groups were planning to create terror in the capital by blowing up the temple- Deputy Commissioner of Police (Special Cell) Alok Kumar told reporters.He did not specify which militant group was planning to carry out the attack");
        /*
         * Resource r3 is describing an incident which is of type of Use of
         * Agent
         */

        Property p3 = model
                .createProperty("http://marathonminds//event3#description");
        resourceUseOfAgent
                .addProperty(
                        p3,
                        "The Nazis first used Zyklon-B poisnous gas to murder people in Auschwitz 64 years ago. The first test, on 20 to 30 Soviet prisoners of war, was carried out in the basement of Block no. 11, in the last days of August, 1941. This experiment was the beginning of the mass extermination that the Germans perpetrated in Auschwitz");

        /*
         * Resource r4 is describing an incident which is of type of Attempted
         * Accquisition
         */

        Property p4 = model
                .createProperty("http://marathonminds//event4#description");
        resourceAttemptedAccquistion
                .addProperty(
                        p4,
                        "In 2000,Usama bin Laden,a leading terrorist, hosted a meeting in Afganistan.The purpose of the meeting was possibly funding the chemical and biological warfare facility in Arab Countries");
        /*
         * Resource r5 is describing an incident which is of type of Threat Only
         */

        Property p5 = model
                .createProperty("http://marathonminds//event5#description");
        resourceThreatOnly
                .addProperty(
                        p5,
                        "On May 4, 2006 India eNews reported ,Security around the Golden Temple - the holiest of Sikh shrines in the world - in Amritsar was beefed up Thursday evening following a threat to blow it up, police said. The threat note, received by the stationmaster of the Jalandhar railway station Thursday, said terrorists would blow up several Sikh and Hindu places of worship across Punjab. ");
        /*
         * Resource r6 is describing an incident which is of type of Threat with
         * Possession
         */

        Property p6 = model
                .createProperty("http://marathonminds//event6#description");
        resourceThreatWithPossession
                .addProperty(
                        p6,
                        "In late August 2003, Andrew  threatened to poison Denmark water supplies with cyanide. Andrew, a 29 year old from Denmark attempted to extort 5000 crowns using the emergency police hotline to convey his demand. Andrew later confessed that his wife had threatened to divorce him if he did not repair the family  financial situation.");

        /*
         * Resource r7 is describing an incident which is of type of Possession
         * Only
         */

        Property p7 = model
                .createProperty("http://marathonminds//event7#description");
        resourcePossessionOnly
                .addProperty(
                        p7,
                        "Two men were arrested Sept. 5 after trying to sell cocaine to RCMP officers of the Municipal Drug Section. RCMP Const. Steve Holmes said 26-year-old Roberto Cabral and 22-year-old Coda Zeleniski were searched after the arrest. Police discovered two bags of powdered cocaine totaling 19 grams, more than $1,200 in cash, a set of brass knuckles, drug scales and 165 capsules of morphine.");

        /*
         * Resource r8 is describing an incident which is of type of False case.
         * This case is directly taken from Montery2RDF
         */

        Property p8 = model
                .createProperty("http://marathonminds//event8#description");
        resourceFalseCase
                .addProperty(
                        p8,
                        "On 13 October 2001, 51 year-old John Silz placed an envelope with finely ground glass on his supervisor's desk. Although Silz intended for the incident to be a joke, his boss promptly reported the envelope, and the suspicious white powder it contained, to the police.  A HazMat team was called onto the scene and the powdered glass was taken in for analysis");

        /*
         * Resource r9 is describing an incident which intially looks like of
         * the type Hoax/Prank ,Plot Only and Use of Agent.But later on it comes
         * only under the category of Plot Only.
         */

        Property p9 = model
                .createProperty("http://marathonminds//event9#description");
        resourcePlotOnly2
                .addProperty(
                        p9,
                        "Andrew was making the fake plan to add Cynide to the water so as to kill many people.But Later on when police caught him because of his fake plan  ,he executed his plan in a perfect manner ");

        /*
         * Resource r10 is describing an incident which intially looks like of
         * the type Plot Only and later on looks like of the type Use of
         * Agent.But still this incident should go to the type of Plot only as
         * Plot Only has major effect then Use of Agent.
         */

        Property p10 = model
                .createProperty("http://marathonminds//even10#description");
        resourcePlotOnly3
                .addProperty(
                        p10,
                        "Terrorist have taken the pictures of the temple and also planned to blow it up.But,some of them were caught by police . Rest of the Terrorist made a plan to use poisnous gases to kill the people. ");
        /*
         * Resource r11 is describing an incident which intially looks like of
         * the type Hoax/Prank and Threat Only.Later on it looks like Plot
         * only.But it should still be Hoax/Prank because it has more effect
         * then any other type of event.
         */

        Property p11 = model
                .createProperty("http://marathonminds//even11#description");
        resourceHoax2
                .addProperty(
                        p11,
                        " A 24-year-old man had been arrested on charges of sending false emails to a news channel threatening to carry out terror strikes in Delhi on the eve of Republic Day. After releasing from the custody he planned to take revenge from the police and therefore he planned to kill police commissioner");

        try {
            str = JenaModelFactory.serializeModel(model, "RDF/XML");

        } catch (Exception e) {
            System.out.println(e);
        }
        return str;
    }

    public static void main(String[] args) throws Exception {
        // TODO pretend persistence... should be removed once we can really
        // persist to blackbook.
        HashMap<String, String> tempDataSource = new HashMap<String, String>();

        // get data from file
        String fileContents = org.apache.commons.io.FileUtils
                .readFileToString(new File(args[0]));

        // get converted data
        InstanceList trainingList = convertRDFToInstanceList(fileContents);

        // train mallet model
        TrainerObject trnObj = trainMalletModel(trainingList);

        // TODO get classifier as rdf
        String classifierToPersistAsRDF = convertClassifierToRDF(trnObj
                .getClassifier());

        // TODO save classifier in blackbook assertions data source
        tempDataSource.putAll(persistClassifierRDF("fakeDataSourceName",
                classifierToPersistAsRDF));

        // TODO retrieve classifier from blackbook assertions data source
        String classifierRetrievedAsRDF = retrieveClassifierRDF(
                "fakeDataSourceName", tempDataSource);

        // TODO get classifier from rdf
        Classifier classifier = convertRDFToClassifier(classifierRetrievedAsRDF);

        /*
         * Model testModel = bbClassify(RDF_HEADER + RDF_HOAX_WITHOUT_LABEL +
         * RDF_THREAT_ONLY_WITHOUT_LABEL + RDF_USE_OF_AGENT_WITHOUT_LABEL +
         * RDF_PLOT_ONLY_WITHOUT_LABEL + RDF_FOOTER, classifier);
         */

        String str = generateTestModel();

        Model testModel = bbClassify(str, classifier);
        // TODO read original test model that now has label answers
        // get data from file
        // testModel.write(System.out,"N-TRIPLE");
        Model answerModel = JenaModelFactory.rdf2Model(args[1]);

        // TODO report should use the built in mechanisms of Mallet for
        // verifying accuracy.
        produceModelAccuracyReport(testModel, answerModel);
    }

    private static InstanceList convertRDFToMalletData(String rdf)
            throws Exception {

        // get converted data
        ByteArrayOutputStream bos = RDF2MalletInstances.convertRDFWithLabels(
                rdf, CLASSIFICATION_PROPERTY.getURI(), null);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        InstanceList iList = (InstanceList) ois.readObject();
        log.error("number of instances retrieved from RDF: " + iList.size());

        return iList;
    }

    private static InstanceList convertRDFToInstanceList(String rdf)
            throws Exception {
        // get converter
        // RDF2MalletInstances conv = new RDF2MalletInstances();

        // get converted data
        ByteArrayOutputStream bos = RDF2MalletInstances.convertRDFWithLabels(
                rdf, CLASSIFICATION_PROPERTY.getURI(), null);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        InstanceList iList = (InstanceList) ois.readObject();
        log.error("number of instances retrieved from RDF: " + iList.size());

        return iList;
    }

    private static TrainerObject trainMalletModel(InstanceList iList) {
        MalletTextDataTrainer bTrainer = new MalletTextDataTrainer();

        TrainerObject trnObj = null;
        try {
            trnObj = bTrainer.train(iList, MalletTextDataTrainer.NAIVE_BAYES);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return trnObj;
    }

    private static String convertClassifierToRDF(Classifier classifier)
            throws Exception {
        Model model = ModelFactory.createDefaultModel();
        Resource res = model.createResource(new URL(
                "http://localhost:8443/blackbook/malletModel_"
                        + System.currentTimeMillis()).toString());
        Property prop = OWL.hasValue;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(classifier);

        Literal obj = model.createTypedLiteral(bos.toByteArray());

        Statement stmt = model.createLiteralStatement(res, prop, obj);
        model.add(stmt);

        String ret = JenaModelFactory.serializeModel(model, "RDF/XML");

        log.error("converted classifier to rdf:");

        return ret;
    }

    // TODO should have a return type of void, not HashMap.
    private static HashMap<String, String> persistClassifierRDF(String dsName,
            String classifierRDF) {
        HashMap<String, String> map = new HashMap<String, String>(1);
        map.put(dsName, classifierRDF);

        log.error("persisted classifier rdf as:");
        return map;
    }

    // TODO this method should not have the data source hashmap passed to it, it
    // should retrieve the classifier from persistent store.
    private static String retrieveClassifierRDF(String dsName,
            HashMap<String, String> tempDataSource) {
        return tempDataSource.get(dsName);
    }

    private static Classifier convertRDFToClassifier(String rdf)
            throws Exception {

        Model model = ModelFactory.createDefaultModel();
        ByteArrayInputStream bisModel = new ByteArrayInputStream(rdf.getBytes());
        model.read(bisModel, "RDF/XML");

        StmtIterator stmtItr = model.listStatements((Resource) null,
                OWL.hasValue, (RDFNode) null);
        Statement onlyStmt = stmtItr.nextStatement();

        ByteArrayInputStream bisLiteral = new ByteArrayInputStream(
                (byte[]) onlyStmt.getLiteral().getValue());
        ObjectInputStream ois = new ObjectInputStream(bisLiteral);
        Classifier classifier = (Classifier) ois.readObject();

        return classifier;

    }

    private static Model bbClassify(String rdf, Classifier classifier)
            throws Exception {

        ByteArrayOutputStream bos = RDF2MalletInstances
                .convertRDFWithoutLabels(rdf, classifier);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        InstanceList iList = (InstanceList) ois.readObject();
        MalletTextClassify malletClassifier = new MalletTextClassify();

        ArrayList<Classification> classifications = malletClassifier.classify(
                classifier, iList);
        ArrayList<MalletAccuracyVector> mAccVectorList = malletClassifier
                .getAccuracyVectors();

        // FIXME needs confidence values as well - Created an empty static
        // method in RDFUtils(in pallet-data)
        // project. - sj
        Model model = RDFUtils.createModelWithClassifications(mAccVectorList,
                classifications);

        // int curIndex = 0;
        // for (Classification c : classifications) {
        // String label = c.getLabeling().getBestLabel().toString();
        // Resource uri = model.createResource((String) c.getInstance()
        // .getName());
        // Statement stmt = model.createStatement(uri,
        // CLASSIFICATION_PROPERTY, label);
        //			
        // //FIXME put in statements about confidence
        //			
        // log.error("adding statment to classification model: " + stmt);
        // model.add(stmt);
        // curIndex++;
        // }

        return model;
    }

    private static void produceModelAccuracyReport(Model testModel,
            Model testModelWithAnswers) throws Exception {
        log.error("model of classifications is: "
                + JenaModelFactory.serializeModel(testModel,
                        FileUtils.langNTriple));
        /*
         * log.error("Hoax entity is marked correctly? " +
         * testModel.contains(HOAX_RESOURCE, CLASSIFICATION_PROPERTY,
         * HOAX_LABEL)); log.error("Threat entity is marked correctly? " +
         * testModel.contains(THREAT_RESOURCE, CLASSIFICATION_PROPERTY,
         * THREAT_LABEL)); log.error("Plot entity is marked correctly? " +
         * testModel.contains(PLOT_RESOURCE, CLASSIFICATION_PROPERTY,
         * PLOT_LABEL)); log.error("Use entity is marked correctly? " +
         * testModel.contains(USE_RESOURCE, CLASSIFICATION_PROPERTY,
         * USE_LABEL));
         */
        NodeIterator stmt = testModel.listObjectsOfProperty(RESOURCE_HOAX,
                BEST_LABEL);
        RDFNode bestLabel = stmt.next();
        Resource resourceBestLabel = ModelFactory.createDefaultModel()
                .createResource(bestLabel.toString());
        log.error("Hoax/Prank entity is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE, HOAX_LABEL));

        stmt = testModel.listObjectsOfProperty(RESOURCE_PLOT_ONLY, BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("Plot only entity is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE,
                        PLOT_ONLY_LABEL));

        stmt = testModel.listObjectsOfProperty(RESOURCE_USE_OF_AGENT,
                BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("Use of agent entity  is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE,
                        USE_OF_AGENT_LABEL));

        stmt = testModel.listObjectsOfProperty(RESOURCE_ATTEMPTED_ACCQUISTION,
                BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("Attempted Accquistion entity is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE,
                        ATTEMPTED_ACCQUISTION_LABEL));

        stmt = testModel
                .listObjectsOfProperty(RESOURCE_THREAT_ONLY, BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("Thread only  entity is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE,
                        THREAT_ONLY_LABEL));

        stmt = testModel.listObjectsOfProperty(RESOURCE_THREAT_WITH_POSSESSION,
                BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("Threat with possession entity is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE,
                        THREAT_WITH_POSSESSION_LABEL));

        stmt = testModel.listObjectsOfProperty(RESOURCE_POSSESSION_ONLY,
                BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("Possession only  entity is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE,
                        POSSESSION_ONLY_LABEL));

        stmt = testModel.listObjectsOfProperty(RESOURCE_FALSE_CASE, BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("False Case entity is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE,
                        FALSE_CASE_LABEL));

        stmt = testModel
                .listObjectsOfProperty(RESOURCE_PLOT_ONLY_2, BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("Plot Only entity is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE,
                        PLOT_ONLY_LABEL));

        stmt = testModel
                .listObjectsOfProperty(RESOURCE_PLOT_ONLY_3, BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("Plot only entity is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE,
                        PLOT_ONLY_LABEL));

        stmt = testModel.listObjectsOfProperty(RESOURCE_HOAX_2, BEST_LABEL);
        bestLabel = stmt.next();
        resourceBestLabel = ModelFactory.createDefaultModel().createResource(
                bestLabel.toString());
        log.error("Hoax/Prank is marked correctly? "
                + testModel.contains(resourceBestLabel, HAS_VALUE, HOAX_LABEL));

    }
}
