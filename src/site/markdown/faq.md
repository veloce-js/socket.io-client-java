# Frequently asked questions

<!-- MACRO{toc} -->

## How to deal with cookies

In order to store the cookies sent by the server and include them in all subsequent requests, you need to create an OkHttpClient with a custom [cookie jar](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-cookie-jar/).

You can either implement your own cookie jar:

```java
public class MyApp {

    public static void main(String[] argz) throws Exception {
        IO.Options options = new IO.Options();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cookieJar(new MyCookieJar())
                .build();

        options.callFactory = okHttpClient;
        options.webSocketFactory = okHttpClient;

        Socket socket = IO.socket(URI.create("https://example.com"), options);

        socket.connect();
    }

    private static class MyCookieJar implements CookieJar {
        private Set<WrappedCookie> cache = new HashSet<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            for (Cookie cookie : cookies) {
                this.cache.add(new WrappedCookie(cookie));
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = new ArrayList<>();
            Iterator<WrappedCookie> iterator = this.cache.iterator();
            while (iterator.hasNext()) {
                Cookie cookie = iterator.next().cookie;
                if (isCookieExpired(cookie)) {
                    iterator.remove();
                } else if (cookie.matches(url)) {
                    cookies.add(cookie);
                }
            }
            return cookies;
        }

        private static boolean isCookieExpired(Cookie cookie) {
            return cookie.expiresAt() < System.currentTimeMillis();
        }
    }

    private static class WrappedCookie {
        private final Cookie cookie;

        public WrappedCookie(Cookie cookie) {
            this.cookie = cookie;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof WrappedCookie)) return false;
            WrappedCookie that = (WrappedCookie) o;
            return that.cookie.name().equals(this.cookie.name())
                    && that.cookie.domain().equals(this.cookie.domain())
                    && that.cookie.path().equals(this.cookie.path())
                    && that.cookie.secure() == this.cookie.secure()
                    && that.cookie.hostOnly() == this.cookie.hostOnly();
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = 31 * hash + cookie.name().hashCode();
            hash = 31 * hash + cookie.domain().hashCode();
            hash = 31 * hash + cookie.path().hashCode();
            hash = 31 * hash + (cookie.secure() ? 0 : 1);
            hash = 31 * hash + (cookie.hostOnly() ? 0 : 1);
            return hash;
        }
    }
}
```

Or use a package like [PersistentCookieJar](https://github.com/franmontiel/PersistentCookieJar):

```java
public class MyApp {

    public static void main(String[] argz) throws Exception {
        IO.Options options = new IO.Options();

        ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();

        options.callFactory = okHttpClient;
        options.webSocketFactory = okHttpClient;

        Socket socket = IO.socket(URI.create("https://example.com"), options);

        socket.connect();
    }
}
```

## How to use with AWS Load Balancing

When scaling to multiple Socket.IO servers, you must ensure that all the HTTP requests of a given session reach the same server (explanation [here](https://socket.io/docs/v4/using-multiple-nodes/#why-is-sticky-session-required)).

Sticky sessions can be enabled on AWS Application Load Balancers, which works by sending a cookie (`AWSALB`) to the client.

Please see [above](#how-to-deal-with-cookies) for how to deal with cookies.

Reference: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/sticky-sessions.html
