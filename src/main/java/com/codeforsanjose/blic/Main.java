package com.codeforsanjose.blic;

import com.codeforsanjose.blic.web.BlicMessage;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import static spark.Spark.get;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("\n"
                + "+-------------------------------------------+\n"
                + "|            Broken Link Checker            |\n"
                + "+-------------------------------------------+");


        if (args.length == 1 && args[0].equalsIgnoreCase("serve")) {
            startWeb();
        } else {
            startCli(args);
        }

    }

    private static void startWeb() {
        get("/check", (req, res) ->{
            res.type("text/json");
            String arg_url = req.queryParams("url");
            Integer arg_depth_limit = pacifistParseInt(req.queryParams("depth"));
            Integer max_thread_limit = pacifistParseInt(req.queryParams("nthreads"));
            Integer arg_fail_tolerance = pacifistParseInt(req.queryParams("nfails"));

            if (arg_url == null || arg_url.length() < 1 ) {
               return new Gson().toJson(new BlicMessage("must provide URL", BlicMessage.types.error));
            }

            arg_depth_limit = (arg_depth_limit == null) ? 3 : arg_depth_limit;
            arg_fail_tolerance = (arg_fail_tolerance == null) ? 3 : arg_fail_tolerance;

            CrawlController c = null;
            try {
                c = new CrawlController(arg_url, arg_depth_limit, arg_fail_tolerance, max_thread_limit);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            c.crawl();
            List<WebPage> results = c.getRawResults();
            return new Gson().toJson(results);

        });
    }

    private static void startCli(String[] args) {
        String arg_url;
        Integer arg_depth_limit = null;
        Integer arg_fail_tolerance = null;
        Integer max_thread_limit = null;
        CrawlController c = null;
        if (args.length == 0) {
            System.out.println(getUsage());
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

        log.debug("args : " + String.join(",", args));

        if (c == null) {
            System.out.println(getUsage());
            System.exit(-1);
        }

        c.crawl();

        List<String> results = c.getResults();
        Collections.sort(results);
        for (String s : results) {
            System.out.println(s);
            log.info(s);
        }
    }

    public static String getUsage() {
        return
                "usage: blic.jar [url] [depth limit] [fail tolerance] [max thread limit]\n"
                        + "\turl:               the URL of a website to be checked for broken links.\n"
                        + "\tdepth limit:       optional number defining how far links should be\n"
                        + "\t                    traversed before stopping\n\n"
                        + "\tfail tolerance:    optional number defining how many retry attempts\n"
                        + "\t                    should be made for a URL that fails to respond in\n"
                        + "\t                    an expected manner.\n\n"
                        + "\tmax thread limit:  optional number that disables the dynamic thread\n"
                        + "\t                    management and defines the max number of threads\n"
                        + "\t                    to be used\n";
    }

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

    /**
     * Try to parse the integer, otherwise shut up.
     */
    static Integer pacifistParseInt(String maybeInt) {
        if (maybeInt == null || maybeInt.length() < 1){
            return null;
        }
        Integer res = null;
        try {
            res = Integer.parseInt(maybeInt);
        } catch (NumberFormatException nfe) {
            // don't careSystem.out.println(nfe);
        }
        return res;
    }

}
