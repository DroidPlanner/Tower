/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.Configuration;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.*;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * @author pabercrombie
 * @version $Id$
 */
// TODO: Android provides a way to test for network connectivity. We could use this to implement isNetworkUnavailable.
public class BasicNetworkStatus extends AVListImpl implements NetworkStatus
{
    protected static final long DEFAULT_TRY_AGAIN_INTERVAL = (long) 60e3; // seconds
    protected static final long NETWORK_STATUS_REPORT_INTERVAL = (long) 120e3;
    protected static final int DEFAULT_ATTEMPT_LIMIT = 7; // number of unavailable events to declare host unavailable
    protected static final String[] DEFAULT_NETWORK_TEST_SITES = new String[]
        {"www.nasa.gov", "worldwind.arc.nasa.gov", "google.com", "microsoft.com", "yahoo.com"};

    protected static class HostInfo
    {
        protected final long tryAgainInterval;
        protected final int attemptLimit;
        protected AtomicInteger logCount = new AtomicInteger();
        protected AtomicLong lastLogTime = new AtomicLong();

        protected HostInfo(int attemptLimit, long tryAgainInterval)
        {
            this.lastLogTime.set(System.currentTimeMillis());
            this.logCount.set(1);
            this.tryAgainInterval = tryAgainInterval;
            this.attemptLimit = attemptLimit;
        }

        protected boolean isUnavailable()
        {
            return this.logCount.get() >= this.attemptLimit;
        }

        protected boolean isTimeToTryAgain()
        {
            return System.currentTimeMillis() - this.lastLogTime.get() >= this.tryAgainInterval;
        }
    }

    // Values exposed to the application.
    // Values exposed to the application.
    protected CopyOnWriteArrayList<String> networkTestSites = new CopyOnWriteArrayList<String>();
    protected AtomicLong tryAgainInterval = new AtomicLong(DEFAULT_TRY_AGAIN_INTERVAL);
    protected AtomicInteger attemptLimit = new AtomicInteger(DEFAULT_ATTEMPT_LIMIT);
    protected boolean offlineMode;

    // Fields for determining and remembering overall network status.
    protected ConcurrentHashMap<String, HostInfo> hostMap = new ConcurrentHashMap<String, HostInfo>();
    protected AtomicLong lastUnavailableLogTime = new AtomicLong(System.currentTimeMillis());
    protected AtomicLong lastAvailableLogTime = new AtomicLong(System.currentTimeMillis() + 1);
    protected AtomicLong lastNetworkCheckTime = new AtomicLong(System.currentTimeMillis());
    protected AtomicLong lastNetworkStatusReportTime = new AtomicLong(0);
    protected AtomicBoolean lastNetworkUnavailableResult = new AtomicBoolean(false);

    public BasicNetworkStatus()
    {
        String oms = Configuration.getStringValue(AVKey.OFFLINE_MODE, "false");
        this.offlineMode = oms.startsWith("t") || oms.startsWith("T");

        this.establishNetworkTestSites();
    }

    /**
     * Determines and stores the network sites to test for public network connectivity. The sites are drawn from the
     * JVM's gov.nasa.worldwind.avkey.NetworkStatusTestSites property ({@link AVKey#NETWORK_STATUS_TEST_SITES}). If that
     * property is not defined, the sites are drawn from the same property in the World Wind or application
     * configuration file. If the sites are not specified there, the set of sites specified in {@link
     * #DEFAULT_NETWORK_TEST_SITES} are used. To indicate an empty list in the JVM property or configuration file
     * property, specify an empty site list, "".
     */
    protected void establishNetworkTestSites()
    {
        String testSites = System.getProperty(AVKey.NETWORK_STATUS_TEST_SITES);

        if (testSites == null)
            testSites = Configuration.getStringValue(AVKey.NETWORK_STATUS_TEST_SITES);

        if (testSites == null)
        {
            this.networkTestSites.addAll(Arrays.asList(DEFAULT_NETWORK_TEST_SITES));
        }
        else
        {
            String[] sites = testSites.split(",");
            List<String> actualSites = new ArrayList<String>(sites.length);

            for (String site : sites)
            {
                site = WWUtil.removeWhiteSpace(site);
                if (!WWUtil.isEmpty(site))
                    actualSites.add(site);
            }

            this.setNetworkTestSites(actualSites);
        }
    }

    public int getAttemptLimit()
    {
        return this.attemptLimit.get();
    }

    public long getTryAgainInterval()
    {
        return this.tryAgainInterval.get();
    }

    public boolean isOfflineMode()
    {
        return this.offlineMode;
    }

    public void setOfflineMode(boolean offlineMode)
    {
        this.offlineMode = offlineMode;
    }

    public void setAttemptLimit(int limit)
    {
        if (limit < 1)
        {
            String message = Logging.getMessage("NetworkStatus.InvalidAttemptLimit");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        this.attemptLimit.set(limit);
    }

    public void setTryAgainInterval(long interval)
    {
        if (interval < 0)
        {
            String message = Logging.getMessage("NetworkStatus.InvalidTryAgainInterval");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        this.tryAgainInterval.set(interval);
    }

    public List<String> getNetworkTestSites()
    {
        return new ArrayList<String>(networkTestSites);
    }

    public void setNetworkTestSites(List<String> networkTestSites)
    {
        this.networkTestSites.clear();

        if (networkTestSites != null)
            this.networkTestSites.addAll(networkTestSites);
    }

    public synchronized void logUnavailableHost(URL url)
    {
        if (this.offlineMode)
            return;

        if (url == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        String hostName = url.getHost();
        HostInfo hi = this.hostMap.get(hostName);
        if (hi != null)
        {
            if (!hi.isUnavailable())
            {
                hi.logCount.incrementAndGet();
                if (hi.isUnavailable()) // host just became unavailable
                    this.firePropertyChange(HOST_UNAVAILABLE, null, url);
            }
            hi.lastLogTime.set(System.currentTimeMillis());
        }
        else
        {
            hi = new HostInfo(this.attemptLimit.get(), this.tryAgainInterval.get());
            hi.logCount.set(1);
            if (hi.isUnavailable()) // the attempt limit may be as low as 1, so handle that case here
                this.firePropertyChange(HOST_UNAVAILABLE, null, url);
            this.hostMap.put(hostName, hi);
        }

        this.lastUnavailableLogTime.set(System.currentTimeMillis());
    }

    public synchronized void logAvailableHost(URL url)
    {
        if (this.offlineMode)
            return;

        if (url == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        String hostName = url.getHost();
        HostInfo hi = this.hostMap.get(hostName);
        if (hi != null)
        {
            this.hostMap.remove(hostName); // host is available again
            firePropertyChange(HOST_AVAILABLE, null, url);
        }

        this.lastAvailableLogTime.set(System.currentTimeMillis());
    }

    public boolean isHostUnavailable(URL url)
    {
        if (this.offlineMode)
            return true;

        if (url == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        String hostName = url.getHost();
        HostInfo hi = this.hostMap.get(hostName);
        if (hi == null)
            return false;

        if (hi.isTimeToTryAgain())
        {
            hi.logCount.set(0); // info removed from table in logAvailableHost
            return false;
        }

        return hi.isUnavailable();
    }

    public boolean isNetworkUnavailable()
    {
        return this.offlineMode || this.isNetworkUnavailable(10000L);
    }

    // TODO: Consider implementing this method using Android's ConnectivityManager
    public synchronized boolean isNetworkUnavailable(long checkInterval)
    {
        if (this.offlineMode)
            return true;

        // If there's been success since failure, network assumed to be reachable.
        if (this.lastAvailableLogTime.get() > this.lastUnavailableLogTime.get())
        {
            this.lastNetworkUnavailableResult.set(false);
            return this.lastNetworkUnavailableResult.get();
        }

        long now = System.currentTimeMillis();

        // If there's been success recently, network assumed to be reachable.
        if (!this.lastNetworkUnavailableResult.get() && now - this.lastAvailableLogTime.get() < checkInterval)
        {
            return this.lastNetworkUnavailableResult.get();
        }

        // If query comes too soon after an earlier one that addressed the network, return the earlier result.
        if (now - this.lastNetworkCheckTime.get() < checkInterval)
        {
            return this.lastNetworkUnavailableResult.get();
        }

        this.lastNetworkCheckTime.set(now);

        if (!this.isWorldWindServerUnavailable())
        {
            this.lastNetworkUnavailableResult.set(false); // network not unreachable
            return this.lastNetworkUnavailableResult.get();
        }

        for (String testHost : networkTestSites)
        {
            if (isHostReachable(testHost))
            {
                {
                    this.lastNetworkUnavailableResult.set(false); // network not unreachable
                    return this.lastNetworkUnavailableResult.get();
                }
            }
        }

        if (now - this.lastNetworkStatusReportTime.get() > NETWORK_STATUS_REPORT_INTERVAL)
        {
            this.lastNetworkStatusReportTime.set(now);
            String message = Logging.getMessage("NetworkStatus.NetworkUnreachable");
            Logging.info(message);
        }

        this.lastNetworkUnavailableResult.set(true); // if no successful contact then network is unreachable
        return this.lastNetworkUnavailableResult.get();
    }

    public boolean isWorldWindServerUnavailable()
    {
        return this.offlineMode || !isHostReachable("worldwind.arc.nasa.gov");
    }

    /**
     * Determine if a host is reachable by attempting to resolve the host name, and then attempting to open a
     * connection.
     *
     * @param hostName Name of the host to connect to.
     *
     * @return {@code true} if a the host is reachable, {@code false} if the host name cannot be resolved, or if opening
     *         a connection to the host fails.
     */
    protected static boolean isHostReachable(String hostName)
    {
        try
        {
            // Assume host is unreachable if we can't get its dns entry without getting an exception
            //noinspection ResultOfMethodCallIgnored
            InetAddress.getByName(hostName);
        }
        catch (UnknownHostException e)
        {
            String message = Logging.getMessage("NetworkStatus.UnreachableTestHost", hostName);
            Logging.verbose(message);
            return false;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("NetworkStatus.ExceptionTestingHost", hostName);
            Logging.verbose(message);
            return false;
        }

        // Was able to get internet address, but host still might not be reachable because the address might have been
        // cached earlier when it was available. So need to try something else.

        URLConnection connection = null;
        try
        {
            URL url = new URL("http://" + hostName);
            Proxy proxy = WWIO.configureProxy();
            if (proxy != null)
                connection = url.openConnection(proxy);
            else
                connection = url.openConnection();

            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            String ct = connection.getContentType();
            if (ct != null)
                return true;
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("NetworkStatus.ExceptionTestingHost", hostName);
            Logging.info(message);
        }
        finally
        {
            if (connection instanceof HttpURLConnection)
                ((HttpURLConnection) connection).disconnect();
        }

        return false;
    }
}
