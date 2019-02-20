package dev.home.johnwhuckle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

public class S3EventHandler implements
                            RequestHandler<S3Event, String>
{
    private static final float MAX_WIDTH = 100;
    private static final float MAX_HEIGHT = 100;
    private final String JPG_TYPE = (String) "jpg";
    private final String JPG_MIME = (String) "image/jpeg";
    private final String PNG_TYPE = (String) "png";
    private final String PNG_MIME = (String) "image/png";

    public String handleRequest (S3Event s3event,
                                 Context context)
    {
        try
        {
            S3EventNotificationRecord record = s3event.getRecords ()
                    .get (0);

            String srcBucket = record.getS3 ()
                    .getBucket ()
                    .getName ();

            // Object key may have spaces or unicode non-ASCII characters.
            String srcKey = record.getS3 ()
                    .getObject ()
                    .getKey ()
                    .replace ('+',
                            ' ');
            System.out.println("Source key: " + srcKey);
            srcKey = URLDecoder.decode (srcKey,
                    "UTF-8");

            String dstBucket = srcBucket + "-resized";
            String dstKey = "resized-" + srcKey;

            System.out.println("Target key: " +  dstKey);

            // Sanity check: validate that source and destination are different
            // buckets.
            if (srcBucket.equals (dstBucket))
            {
                System.out
                        .println ("Destination bucket must not match source bucket.");
                return "";
            }
            //BasicAWSCredentials creds = new BasicAWSCredentials("access_key", "secret_key");
            System.out.println(String.format("Copying %s from %s to %s", srcKey, srcBucket, dstBucket));
            AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();
            s3.copyObject ( srcBucket, srcKey, dstBucket, srcKey);
            s3.deleteObject (srcBucket,srcKey);

            return "OK";

        }
        catch (IOException e)
        {
            throw new RuntimeException (e);
        }
    }
}
