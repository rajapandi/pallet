package pallet.test.util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;

import security.ejb.client.User;
import blackbook.metadata.manager.MetadataManagerFactory;
import blackbook.metadata.manager.MetadataManagerFromMemory;

/**
 * Various utilities for JUnit testing
 * 
 * 
 */
@Ignore("This is a utility class, not a real test.")
public class TestUtilities {
    /**
     * cert chain with 3 certs
     */
    public static final String BASE64ENCODING_3CERTS =

    "-----BEGIN CERTIFICATE-----\n"
            + "MIIE3jCCA8agAwIBAgIJALl4DTL0+A8VMA0GCSqGSIb3DQEBBQUAMIGfMQswCQYD\n"
            + "VQQGEwJVUzERMA8GA1UECBMITWFyeWxhbmQxEjAQBgNVBAcTCUJhbHRpbW9yZTEM\n"
            + "MAoGA1UEChMDa2RkMQ0wCwYDVQQLEwRiZGFkMSQwIgYDVQQDExtCREFEIEJhbHRp\n"
            + "bW9yZSBMb2NhbGl6ZWQgQ0ExJjAkBgkqhkiG9w0BCQEWF2JhbHRpbW9yZUBiYWx0\n"
            + "aW1vcmUuY29tMB4XDTA4MDMyNzE5NDg1OVoXDTEzMDMyNjE5NDg1OVowgZ8xCzAJ\n"
            + "BgNVBAYTAlVTMREwDwYDVQQIEwhNYXJ5bGFuZDESMBAGA1UEBxMJQmFsdGltb3Jl\n"
            + "MQwwCgYDVQQKEwNrZGQxDTALBgNVBAsTBGJkYWQxJTAjBgNVBAMTHEJsYWNrYm9v\n"
            + "ayBUZXN0IFVzZXIgdGVzdHVzZXIxJTAjBgkqhkiG9w0BCQEWFnRlc3R1c2VyQGJh\n"
            + "bHRpbW9yZS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZIAqA\n"
            + "1rIgZ/Ij172GHcW5/tqYYhHl5SzPAYh8bODdAY8ZvTM2R7mR+e9Rtvopase+BMZh\n"
            + "Zq6B30awnAlI/7G0lZzAKipKLD7/galDJSm6zD8Q/CJ/ztIxIZyT+7SD5WFGHwhS\n"
            + "M7nvXqiG07NwN718aWCXRzpRE8KyHdjDox7bD6mAZf/wpzzaHifyJMtoLE++VZU6\n"
            + "wKfl7u7GZcQf8v+MhZNnXJXHS1prRXs/Wmd3l2FQDAc+nf6vcf0bnG8W8xDnejyB\n"
            + "cz9TAVKMo6ToQO1aPNGN1jTgPHliEmXQZtPYexYxgJhSH2HR2zuRg8UIl1to1XZf\n"
            + "VZLo4xHNRQkuz3/1AgMBAAGjggEZMIIBFTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIB\n"
            + "DQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUashi\n"
            + "hvHmGqOUAyH43b+32wzUSbwwgboGA1UdIwSBsjCBr4AUONRZJcfIkzpJuAVQnmGI\n"
            + "DZccGJmhgYukgYgwgYUxCzAJBgNVBAYTAlVTMQwwCgYDVQQIEwNBbGwxDDAKBgNV\n"
            + "BAcTA0FsbDEMMAoGA1UEChMDS0REMQ0wCwYDVQQLEwRCREFEMRYwFAYDVQQDEw1C\n"
            + "REFEIFBLSSBSb290MSUwIwYJKoZIhvcNAQkBFhZuYXRpb25hbEBqMmVlLnJhYmEu\n"
            + "Y29tggkA530IXj1YDGUwDQYJKoZIhvcNAQEFBQADggEBADTvmg1W+XAhbGOriP8t\n"
            + "Hl1uFzwPd+BjGs3HmGpzvPWmh52dlHMLYTvfejsyEYEkJLQwsmtLmnrQHxz6KWUd\n"
            + "Tlr4oemCdZYaB+8Nu7uYmnR5ejw4K/Ns/T4+y6l9QXJTeQk5JRClqUspnb7DseMN\n"
            + "H4UOOCGCTMRKQYWqvEnvmC/K3YDSoRLYd2fClVwIh4dI9b+BtsF/sl6jBZG5OHVo\n"
            + "eaoXG2KvxDnNL7VpyFLt6KWHk/P6elWE+km5489X+5P8nC1itn4roMbib2OM4FS+\n"
            + "JKoek9x9bBIDES/waUJaijOlN6R1+EPuVYCAD4wKqxKROvgO6J2W3sao6P1SVpTG\n"
            + "hs0=\n"
            + "-----END CERTIFICATE-----\n"
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIIEKDCCAxCgAwIBAgIJAOd9CF49WAxlMA0GCSqGSIb3DQEBBQUAMIGFMQswCQYD\n"
            + "VQQGEwJVUzEMMAoGA1UECBMDQWxsMQwwCgYDVQQHEwNBbGwxDDAKBgNVBAoTA0tE\n"
            + "RDENMAsGA1UECxMEQkRBRDEWMBQGA1UEAxMNQkRBRCBQS0kgUm9vdDElMCMGCSqG\n"
            + "SIb3DQEJARYWbmF0aW9uYWxAajJlZS5yYWJhLmNvbTAeFw0wNjA3MDcxODMzMDBa\n"
            + "Fw0xMjAyMTQxODMzMDBaMIGfMQswCQYDVQQGEwJVUzERMA8GA1UECBMITWFyeWxh\n"
            + "bmQxEjAQBgNVBAcTCUJhbHRpbW9yZTEMMAoGA1UEChMDa2RkMQ0wCwYDVQQLEwRi\n"
            + "ZGFkMSQwIgYDVQQDExtCREFEIEJhbHRpbW9yZSBMb2NhbGl6ZWQgQ0ExJjAkBgkq\n"
            + "hkiG9w0BCQEWF2JhbHRpbW9yZUBiYWx0aW1vcmUuY29tMIIBIjANBgkqhkiG9w0B\n"
            + "AQEFAAOCAQ8AMIIBCgKCAQEAoamZ/U1SCj6y2F0Q70qIbY0ChbpuOZId/um5ZGPs\n"
            + "VKP285zbX5GaTlXtpGZBB4cHs+RnJuQhL0vSM9uSYdaHXa083Jh+ulFFOHvsaJg5\n"
            + "yvaVOe8p2kHLANjdDj9hPJ0cnO1krHopz9RfpC1JkSxZgV+6deQutSa7V7omvPmS\n"
            + "X+Q/cpTe4GTWWcmFZfVACwkVxKLAeda3l7upAu+ElDYduE4AdwOq0kb62SNKnno9\n"
            + "huFVo01qBbdBrDcnu/Et7a9Chti7LzdFh4zmDDdV2cmNmrFed+oTt+JR2S5eTqnF\n"
            + "ohuMkXJtNH1qUI7rletivG1VuyI/sYootcOgJnpzMIjJBwIDAQABo38wfTAdBgNV\n"
            + "HQ4EFgQUONRZJcfIkzpJuAVQnmGIDZccGJkwHwYDVR0jBBgwFoAUF0fj8JQgivMQ\n"
            + "YzHbgr98pTggvaEwFwYDVR0gBBAwDjAFBgMqBAUwBQYDLSwBMA4GA1UdDwEB/wQE\n"
            + "AwIBhjASBgNVHRMBAf8ECDAGAQH/AgECMA0GCSqGSIb3DQEBBQUAA4IBAQBLKm7Z\n"
            + "4GXBiUcJ6DrghJK/8Jv5aq8Lkatob4CXHZ/+nXuI29tUo8wf/M/RtlkXo+e0piVh\n"
            + "mLC7Ey/PUaz0MI/HPhEWFjszAuFx1xACJ0WYPYJbFfvtX3PJCZyv1zFIiY5mFBy1\n"
            + "nBsv20M8Mj/zGk6Vl1OZ5+oiIklHuzMVZxTWkfV0TxbHnf29zi+auo77hA+x6rOx\n"
            + "4LQyBd0FHmIIPcpeFm8FxAfPCdpG25aGZL8TE/MSldIC1CNzNSz8bFlLYsR97s/E\n"
            + "CtX1hWTVCcTr/3ITSyzAS6gkUMp5h3CMyTjRrccmAtaJZkPNYoGpZ4AhWE7nCov0\n"
            + "jwq4bKDonXrLjqBO\n"
            + "-----END CERTIFICATE-----\n"
            + "-----BEGIN TRUSTED CERTIFICATE-----\n"
            + "MIID5TCCAs2gAwIBAgIJAJ79cyHj6CtkMA0GCSqGSIb3DQEBBAUAMIGFMQswCQYD\n"
            + "VQQGEwJVUzEMMAoGA1UECBMDQWxsMQwwCgYDVQQHEwNBbGwxDDAKBgNVBAoTA0tE\n"
            + "RDENMAsGA1UECxMEQkRBRDEWMBQGA1UEAxMNQkRBRCBQS0kgUm9vdDElMCMGCSqG\n"
            + "SIb3DQEJARYWbmF0aW9uYWxAajJlZS5yYWJhLmNvbTAeFw0wNjA3MDcxODMyNTla\n"
            + "Fw0xMjAyMTQxODMyNTlaMIGFMQswCQYDVQQGEwJVUzEMMAoGA1UECBMDQWxsMQww\n"
            + "CgYDVQQHEwNBbGwxDDAKBgNVBAoTA0tERDENMAsGA1UECxMEQkRBRDEWMBQGA1UE\n"
            + "AxMNQkRBRCBQS0kgUm9vdDElMCMGCSqGSIb3DQEJARYWbmF0aW9uYWxAajJlZS5y\n"
            + "YWJhLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMDfdy+iiKH4\n"
            + "ORtXDwPAz+py4Bf9TwyiG+SZwLABFrTWBHaNcafYamXJ0RnNLDShS0Jf6ANtkEN5\n"
            + "HyFhJ+C4mC3L4Lnz7EgmieMAHMP/ngWwQ1NFmJKZafx92bFiPM5Y860Masnw+mTk\n"
            + "SDP8XSun5Jl6A1BROyWZb6D6CRmAfdYMceL7a+7ooo+gYTfMuIKzzQYbPOWdbrV/\n"
            + "xEjTFPsjVaTSPYGln+8PalANVYaZzfLpmjLEb9Lk0KUDqtvAi5js54Njy42PUz+k\n"
            + "Lbj77RZ2Q86NqZViGN4rwVADbq9XhqPeL0F4qwg5PH/r9O7gohjBSRwsQSYgqFxI\n"
            + "BSOdKaczq28CAwEAAaNWMFQwEgYDVR0TAQH/BAgwBgEB/wIBAzAdBgNVHQ4EFgQU\n"
            + "F0fj8JQgivMQYzHbgr98pTggvaEwHwYDVR0jBBgwFoAUF0fj8JQgivMQYzHbgr98\n"
            + "pTggvaEwDQYJKoZIhvcNAQEEBQADggEBADNMX/ar5JRX6eK23+FeCDBg3BpmdYBh\n"
            + "+wgyffgarDHQWA8YRwKy4OPUGkcSljjx0vadeNZDlkNTGfONFUm2YC0sWBwObFJn\n"
            + "fLSRRyOQn4N92KhAImaqiJVqaygm31a/XmfbJAHRLVsrAuSeR59axoMXbojFxgru\n"
            + "fag7ZA0CmIEn2jrgzn70HmSenhLK7MWpdP35Q6W4xEXW04bCwrXL6MF4njkbWJmP\n"
            + "JMkmGAZjO85iDRUGWPYTST6LpTwT+8LRawxY92D5vPsLd6r0svLTpDmtA3Q7HrCP\n"
            + "WsAsY7czKNPy1deCPKhq8+tsAmyu9nWlB4XmJkW3Zkic92oR+uTQwy0=\n"
            + "-----END TRUSTED CERTIFICATE-----\n";

    /**
     * cert chain with 2 certs
     */
    public static final String BASE64ENCODING_2CERTS = "-----BEGIN TRUSTED CERTIFICATE-----\n"
            + "MIIE3jCCA8agAwIBAgIJALl4DTL0+A8VMA0GCSqGSIb3DQEBBQUAMIGfMQswCQYD\n"
            + "VQQGEwJVUzERMA8GA1UECBMITWFyeWxhbmQxEjAQBgNVBAcTCUJhbHRpbW9yZTEM\n"
            + "MAoGA1UEChMDa2RkMQ0wCwYDVQQLEwRiZGFkMSQwIgYDVQQDExtCREFEIEJhbHRp\n"
            + "bW9yZSBMb2NhbGl6ZWQgQ0ExJjAkBgkqhkiG9w0BCQEWF2JhbHRpbW9yZUBiYWx0\n"
            + "aW1vcmUuY29tMB4XDTA4MDMyNzE5NDg1OVoXDTEzMDMyNjE5NDg1OVowgZ8xCzAJ\n"
            + "BgNVBAYTAlVTMREwDwYDVQQIEwhNYXJ5bGFuZDESMBAGA1UEBxMJQmFsdGltb3Jl\n"
            + "MQwwCgYDVQQKEwNrZGQxDTALBgNVBAsTBGJkYWQxJTAjBgNVBAMTHEJsYWNrYm9v\n"
            + "ayBUZXN0IFVzZXIgdGVzdHVzZXIxJTAjBgkqhkiG9w0BCQEWFnRlc3R1c2VyQGJh\n"
            + "bHRpbW9yZS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZIAqA\n"
            + "1rIgZ/Ij172GHcW5/tqYYhHl5SzPAYh8bODdAY8ZvTM2R7mR+e9Rtvopase+BMZh\n"
            + "Zq6B30awnAlI/7G0lZzAKipKLD7/galDJSm6zD8Q/CJ/ztIxIZyT+7SD5WFGHwhS\n"
            + "M7nvXqiG07NwN718aWCXRzpRE8KyHdjDox7bD6mAZf/wpzzaHifyJMtoLE++VZU6\n"
            + "wKfl7u7GZcQf8v+MhZNnXJXHS1prRXs/Wmd3l2FQDAc+nf6vcf0bnG8W8xDnejyB\n"
            + "cz9TAVKMo6ToQO1aPNGN1jTgPHliEmXQZtPYexYxgJhSH2HR2zuRg8UIl1to1XZf\n"
            + "VZLo4xHNRQkuz3/1AgMBAAGjggEZMIIBFTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIB\n"
            + "DQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUashi\n"
            + "hvHmGqOUAyH43b+32wzUSbwwgboGA1UdIwSBsjCBr4AUONRZJcfIkzpJuAVQnmGI\n"
            + "DZccGJmhgYukgYgwgYUxCzAJBgNVBAYTAlVTMQwwCgYDVQQIEwNBbGwxDDAKBgNV\n"
            + "BAcTA0FsbDEMMAoGA1UEChMDS0REMQ0wCwYDVQQLEwRCREFEMRYwFAYDVQQDEw1C\n"
            + "REFEIFBLSSBSb290MSUwIwYJKoZIhvcNAQkBFhZuYXRpb25hbEBqMmVlLnJhYmEu\n"
            + "Y29tggkA530IXj1YDGUwDQYJKoZIhvcNAQEFBQADggEBADTvmg1W+XAhbGOriP8t\n"
            + "Hl1uFzwPd+BjGs3HmGpzvPWmh52dlHMLYTvfejsyEYEkJLQwsmtLmnrQHxz6KWUd\n"
            + "Tlr4oemCdZYaB+8Nu7uYmnR5ejw4K/Ns/T4+y6l9QXJTeQk5JRClqUspnb7DseMN\n"
            + "H4UOOCGCTMRKQYWqvEnvmC/K3YDSoRLYd2fClVwIh4dI9b+BtsF/sl6jBZG5OHVo\n"
            + "eaoXG2KvxDnNL7VpyFLt6KWHk/P6elWE+km5489X+5P8nC1itn4roMbib2OM4FS+\n"
            + "JKoek9x9bBIDES/waUJaijOlN6R1+EPuVYCAD4wKqxKROvgO6J2W3sao6P1SVpTG\n"
            + "hs0=\n"
            + "-----END TRUSTED CERTIFICATE-----\n"
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIIEKDCCAxCgAwIBAgIJAOd9CF49WAxlMA0GCSqGSIb3DQEBBQUAMIGFMQswCQYD\n"
            + "VQQGEwJVUzEMMAoGA1UECBMDQWxsMQwwCgYDVQQHEwNBbGwxDDAKBgNVBAoTA0tE\n"
            + "RDENMAsGA1UECxMEQkRBRDEWMBQGA1UEAxMNQkRBRCBQS0kgUm9vdDElMCMGCSqG\n"
            + "SIb3DQEJARYWbmF0aW9uYWxAajJlZS5yYWJhLmNvbTAeFw0wNjA3MDcxODMzMDBa\n"
            + "Fw0xMjAyMTQxODMzMDBaMIGfMQswCQYDVQQGEwJVUzERMA8GA1UECBMITWFyeWxh\n"
            + "bmQxEjAQBgNVBAcTCUJhbHRpbW9yZTEMMAoGA1UEChMDa2RkMQ0wCwYDVQQLEwRi\n"
            + "ZGFkMSQwIgYDVQQDExtCREFEIEJhbHRpbW9yZSBMb2NhbGl6ZWQgQ0ExJjAkBgkq\n"
            + "hkiG9w0BCQEWF2JhbHRpbW9yZUBiYWx0aW1vcmUuY29tMIIBIjANBgkqhkiG9w0B\n"
            + "AQEFAAOCAQ8AMIIBCgKCAQEAoamZ/U1SCj6y2F0Q70qIbY0ChbpuOZId/um5ZGPs\n"
            + "VKP285zbX5GaTlXtpGZBB4cHs+RnJuQhL0vSM9uSYdaHXa083Jh+ulFFOHvsaJg5\n"
            + "yvaVOe8p2kHLANjdDj9hPJ0cnO1krHopz9RfpC1JkSxZgV+6deQutSa7V7omvPmS\n"
            + "X+Q/cpTe4GTWWcmFZfVACwkVxKLAeda3l7upAu+ElDYduE4AdwOq0kb62SNKnno9\n"
            + "huFVo01qBbdBrDcnu/Et7a9Chti7LzdFh4zmDDdV2cmNmrFed+oTt+JR2S5eTqnF\n"
            + "ohuMkXJtNH1qUI7rletivG1VuyI/sYootcOgJnpzMIjJBwIDAQABo38wfTAdBgNV\n"
            + "HQ4EFgQUONRZJcfIkzpJuAVQnmGIDZccGJkwHwYDVR0jBBgwFoAUF0fj8JQgivMQ\n"
            + "YzHbgr98pTggvaEwFwYDVR0gBBAwDjAFBgMqBAUwBQYDLSwBMA4GA1UdDwEB/wQE\n"
            + "AwIBhjASBgNVHRMBAf8ECDAGAQH/AgECMA0GCSqGSIb3DQEBBQUAA4IBAQBLKm7Z\n"
            + "4GXBiUcJ6DrghJK/8Jv5aq8Lkatob4CXHZ/+nXuI29tUo8wf/M/RtlkXo+e0piVh\n"
            + "mLC7Ey/PUaz0MI/HPhEWFjszAuFx1xACJ0WYPYJbFfvtX3PJCZyv1zFIiY5mFBy1\n"
            + "nBsv20M8Mj/zGk6Vl1OZ5+oiIklHuzMVZxTWkfV0TxbHnf29zi+auo77hA+x6rOx\n"
            + "4LQyBd0FHmIIPcpeFm8FxAfPCdpG25aGZL8TE/MSldIC1CNzNSz8bFlLYsR97s/E\n"
            + "CtX1hWTVCcTr/3ITSyzAS6gkUMp5h3CMyTjRrccmAtaJZkPNYoGpZ4AhWE7nCov0\n"
            + "jwq4bKDonXrLjqBO\n" + "-----END CERTIFICATE-----\n";

    /** test base URI */
    public static final String TEST_BASE_URI = "urn:test:";

    /** test model name */
    public static final String TEST_MODEL_NAME = "test";

    /**
     * Deletes all sub-directories and files underneath the supplied directory.
     * It wont delete the dir supplied as a parameter.
     * 
     * @param dir
     */
    public static void deleteAllSubDirectoriesAndFiles(File dir) {
        File indexDirectory = dir;
        File[] files = indexDirectory.listFiles();

        for (int i = 0; i < files.length; i++) {

            TestUtilities.deleteDir(files[i]);
        }
    }

    /**
     * Deletes files in a directory. This will delete the File/Directory
     * supplied as the parameter, too.
     * 
     * @param dir
     * @return if file successfully deleted
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * Get hostname of the local machine
     * 
     * @return hostName
     */
    public static String getHostName() {
        String hostName = null;

        try {
            InetAddress localMachine = InetAddress.getLocalHost();

            hostName = localMachine.getHostName();
        } catch (UnknownHostException uhe) {

        }
        return hostName;
    }

    /**
     * Get a security.ejb.client.User
     * 
     * @return user - user making the request in the tests
     */
    public static User getUser() {
        Set<String> rolesSet = new HashSet<String>();
        rolesSet.add("ROLE3");
        rolesSet.add("UNCLASSIFIED");
        rolesSet.add("ROLE2");
        rolesSet.add("ROLE1");
        rolesSet.add("ROLE4");
        rolesSet.add("ROLE5");

        User user = new User();
        user.setId("9");
        user.setLogin("testuser");
        user.setRoles(rolesSet);

        return user;
    }

    /**
     * Clears various caches and sets the MetadataManager instance to
     * MetadataManagerFromMemory.
     */
    public static void prepareForTesting() {
        MetadataManagerFromMemory.clearCache();
        MetadataManagerFactory.setClass(MetadataManagerFromMemory.class);
    }
}
