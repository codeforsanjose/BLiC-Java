package com.codeforsanjose.blic;

import java.net.MalformedURLException;

public class Main {


    public static void main(String[] args) {
        System.out.println("Broken Link Checker");

        String arg_url;
        Integer arg_depth_limit = null;
        Integer arg_fail_tolerance = null;
        Integer max_thread_limit = null;
        CrawlController c = null;

        if (args.length == 0) {
            System.out.println("Please provide the URL of a website to be checked for broken links.");
            System.out.println("The URL would be the first argument passed into the program when run in the command line.");
            System.out.println("Example usage:");
            System.out.println("java -jar blic.jar http://codeforsanjose.com/");
            System.exit(-1);
        } else if (args.length == 1) {
            arg_url = args[0];
            arg_depth_limit = 3;
            try {
                c = new CrawlController(arg_url, arg_depth_limit);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else if (args.length == 2) {
            arg_url = args[0];
            arg_depth_limit = parseArgInt(args, 1, "Second argument (depth limit) must be a valid integer");
            try {
                c = new CrawlController(arg_url, arg_depth_limit);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else if (args.length == 3) {
            arg_url = args[0];
            arg_depth_limit = parseArgInt(args, 1, "Second argument (depth limit) must be a valid integer");
            arg_fail_tolerance = parseArgInt(args, 2, "Third argument (fail tolerance) must be a valid integer");
            try {
                c = new CrawlController(arg_url, arg_depth_limit, arg_fail_tolerance);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else if (args.length == 4) {
            arg_url = args[0];
            arg_depth_limit = parseArgInt(args, 1, "Second argument (depth limit) must be a valid integer");
            arg_fail_tolerance = parseArgInt(args, 2, "Third argument (fail tolerance) must be a valid integer");
            max_thread_limit = parseArgInt(args, 3, "Third argument (max thread limit) must be a valid integer");
            try {
                c = new CrawlController(arg_url, arg_depth_limit, arg_fail_tolerance, max_thread_limit);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        if (c == null) {
            System.exit(-1);
        }

        c.crawl();
        for (String s : c.getResults()) {
            System.out.println(s);
        }
    }

    ;

    /**
     * Try to parse an integer from args at position argn
     *
     * @param args       the array of strings containing the number to be parsed
     * @param argn       array position in args that should be parsed
     * @param errmessage message to be written to the console if parsing the integer fails
     * @return null if unable to parse the int from the given String
     */
    static Integer parseArgInt(String[] args, int argn, String errmessage) {
        Integer res = null;
        try {
            res = Integer.parseInt(args[argn]);
        } catch (NumberFormatException nfe) {
            System.out.println(errmessage);
        }
        return res;
    }

}
