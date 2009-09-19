package utd.pallet.data;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * commandLineParser is a class which will parse the command line arguments .
 * for eg Train-dir C://user,d://home NaiveBayes a.txt@Train-file b.txt,c.txt
 * MaxEnt d.txt@Validation-dir d://user,c://user There are three commands. The
 * parser will convert it like firstToken=Train-dir ArrayList contains C://user
 * d://user ThirdToken=NaiveBayes fourthToken a.txt. All these are added to one
 * object of commandLineParser. Therefore all the commands are in an ArrayList
 * of commandLineParser.Please give @ between two commands and , between two
 * files and directory name.
 */
public class commandLineParser {
    /**
     * It takes the firstToken(Train-dir)
     */
    public String firstToken;
    /**
     * It takes the list of directories or text file
     */
    public ArrayList<String> secondToken = new ArrayList<String>();
    /**
     * It takes the third token of arguments
     */
    public String thirdToken;
    /**
     * It takes the fourth token of arguments.
     */
    public String fourthToken;

    /**
     * @param args
     */
    public static ArrayList<commandLineParser> argumentParser(String args[]) {
        // public static void main(String args[]) {
        int totalCommands = args.length;

        int k = 0;
        String str = "";
        int p1 = totalCommands;
        while (p1 != 0) {

            str = str + args[k] + " ";
            k++;
            p1--;
        }
        System.out.println(str);
        StringTokenizer commandLineArguments = new StringTokenizer(str, "@");
        ArrayList<commandLineParser> commandParser = new ArrayList<commandLineParser>();

        while (commandLineArguments.hasMoreTokens()) {
            StringTokenizer completeCommand = new StringTokenizer(
                    commandLineArguments.nextToken(), " ");
            int size = completeCommand.countTokens();
            System.out.println(size);
            commandLineParser ok = new commandLineParser();
            ok.firstToken = null;
            ok.thirdToken = null;
            ok.fourthToken = null;
            ok.firstToken = completeCommand.nextToken();

            StringTokenizer argumentSecond = new StringTokenizer(
                    completeCommand.nextToken(), ",");

            while (argumentSecond.hasMoreTokens()) {
                ok.secondToken.add(argumentSecond.nextToken());
            }
            if (size == 3) {
                String s1 = completeCommand.nextToken();
                ok.thirdToken = s1;
            }
            if (size == 4) {
                String s1 = completeCommand.nextToken();
                ok.thirdToken = s1;
                String s2 = completeCommand.nextToken();
                ok.fourthToken = s2;
            }
            commandParser.add(ok);
        }
        return commandParser;

    }
}
