<h1>SPRING DELIVERY ENGINE</h1>
<p>
  <b>Spring Delivery Engine</b> is a modern, high-performance hybrid logistics, courier dispatch, and last-mile delivery engine for gastronomy. The engine handles multi-item carts, cash-on-delivery (COD) balancing, passwordless OTP authentication with single-active-session enforcement, event-driven transactional mail notifications, and real-time courier GPS tracking via Apache Kafka. The architecture isolates persistent business entities (PostgreSQL) from high-frequency write streams and asynchronous background execution (Redis in-memory storage & Apache Kafka event bus).
</p>

<h2>System Capabilities</h2>
<p>
  <b>Hybrid & Event-Driven Architecture:</b> Combines PostgreSQL for relational domain entities, Redis for transient in-memory telemetry/sessions, and Apache Kafka for decoupled, non-blocking event streaming.<br>
  <b>Low-Latency Async Event Processing (Apache Kafka):</b> Offloads email generation and cache eviction to background consumers (<code>OrderEventsConsumer</code>, <code>CourierLocationConsumer</code>), keeping REST API response times under milliseconds.<br>
  <b>Real-Time Geospatial Tracking (Redis Geo & WebSockets):</b> Ingests GPS telemetry into Redis Geo (<code>GEOADD</code>/<code>GEOPOS</code>) and broadcasts live coordinate streams to clients via WebSocket topics.<br>
  <b>Employee Privacy Protection:</b> Restricts access to courier location data, allowing customers to track live movement strictly while the delivery status is set to <code>ON_THE_WAY</code>.<br>
  <b>Lifecycle Management & Duration Analytics:</b> Manages the stateful fulfillment process (<code>PENDING</code> &rarr; <code>PREPARING</code> &rarr; <code>ON_THE_WAY</code> &rarr; <code>DELIVERED</code>) while dynamically calculating total fulfillment duration.<br>
  <b>Asynchronous Transactional Emails:</b> Triggers lifecycle notifications (order confirmation, payment receipts, driver dispatch, delivery with rating link, cancellation) in background threads via Kafka event listeners.<br>
  <b>Cash-on-Delivery (COD) Balance System:</b> Tracks cash collected by couriers for <code>PAY_BY_CASH</code> orders directly on the <code>Courier</code> entity, offering flexible regulation (<code>regulateBalance</code>) via JPA Dirty Checking.<br>
  <b>Automated Memory Eviction & Token Blacklisting:</b> Asynchronously purges courier location data from Redis upon order delivery/cancellation and blacklists JWTs upon logout or multi-device login overrides.<br>
  <b>Passwordless OTP & Single Active Session:</b> Features a 6-digit OTP authentication flow (5-minute TTL) enforcing a strict single-device active session policy per courier.<br>
  <b>Feedback Loop & Rating System (Client Rate):</b> Generates unique evaluation links (<code>/rate</code>) sent via transactional emails to prevent duplicate ratings per order.<br>
  <b>Centralized Exception Handling:</b> Catches domain errors and validation failures globally via <code>@RestControllerAdvice</code>, returning clean JSON error responses.<br>
  <b>Pre-seeded Test Data (DataInitializer):</b> Automatically populates the database with sample records upon startup for immediate API testing in Postman.
</p>

<h2>Tech Stack</h2>
<p>
  <b>Language & Framework:</b> Java 26 / Spring Boot 3.x (Web, Data JPA, Data Redis, Security, Validation, JavaMailSender, Spring Kafka)<br>
  <b>Messaging & Streaming:</b> Apache Kafka (Spring Kafka, Custom DTO Event Records)<br>
  <b>Databases:</b> PostgreSQL 16 & Redis 7 (Lettuce Driver, StringRedisTemplate)<br>
  <b>Real-Time Communication:</b> Spring WebSocket (STOMP broker / SimpMessagingTemplate)<br>
  <b>Security & Tokens:</b> Spring Security 6 (Stateless JWT, HttpOnly Cookies, Custom JWTFilter)<br>
  <b>ORM & Tooling:</b> Hibernate 6/7 / JPA (UUID Primary Keys, Unidirectional One-To-Many), Lombok, Docker, Maven
</p>

<h2>Architecture & Technical Solutions</h2>
<p>
  <b>1. Asynchronous Event-Driven Decoupling (OrderEvent & CourierLocationEvent)</b><br>
  Domain events are encapsulated into immutable Java records (<code>OrderEvent</code>, <code>CourierLocationEvent</code>) using static factory <code>.from()</code> methods. This prevents JPA lazy-initialization errors and Jackson circular reference loops during Kafka serialization.
</p>
<p>
  <b>2. In-Memory Telemetry & Real-Time Broadcasting</b><br>
  The endpoint <code>POST /api/db/couriers/location</code> emits location payloads directly to Kafka topic <code>courier-locations</code>. The <code>CourierLocationConsumer</code> asynchronously writes coordinates to Redis Geo and broadcasts live updates over WebSocket destination <code>/topic/courier/{id}/location</code>.
</p>
<p>
  <b>3. Non-Blocking Order Lifecycle Management</b><br>
  Order state transitions (<code>placeOrder</code>, <code>payOrder</code>, <code>orderOnTheWay</code>, <code>orderDelivered</code>, <code>orderCancelled</code>) persist status changes in PostgreSQL and immediately publish an <code>OrderEvent</code> to topic <code>order-events</code>. Heavy tasks such as SMTP mail dispatch and Redis cache purges are delegated to <code>OrderEventsConsumer</code>.
</p>
<p>
  <b>4. Contextual GPS Access Control (trackOrder)</b><br>
  The <code>GET /api/orders/{id}/track</code> endpoint verifies the order status in PostgreSQL first. Redis is queried via <code>GEOPOS</code> exclusively when the order status is <code>ON_THE_WAY</code>.
</p>
<p>
  <b>5. Delivery Operations, Cancellation & Balance Regulation</b><br>
  Transitioning an order to <code>DELIVERED</code> calculates fulfillment duration, credits cash for <code>PAY_BY_CASH</code> orders to the courier's balance, emits a Kafka event to send a feedback email, restores courier status to <code>AVAILABLE</code>, and asynchronously clears Redis location cache.
</p>

<h2>Installation & Configuration</h2>
<p>
  <b>1. Prerequisites</b><br>
  Ensure you have <b>JDK 26</b>, <b>Maven</b>, and <b>Docker Desktop</b> installed on your environment.
</p>
<p>
  <b>2. Start PostgreSQL, Redis & Kafka via Docker</b><br>
  Run the infrastructure containers:
</p>
<pre><code>docker compose up -d</code></pre>
<p>
  <b>3. Run the Application</b><br>
  Build and start the Spring Boot backend using Maven:
</p>
<pre><code>mvn clean spring-boot:run</code></pre>
<p>
  Once started, the backend API will be available at <code>http://localhost:8080/api/</code> for Postman requests.
</p>

<hr>

<h1>SPRING DELIVERY ENGINE</h1>
<p>
  <b>Spring Delivery Engine</b> to hybrydowy, wydajny silnik logistyczny i dyspozytorski last-mile delivery dla gastronomii. System obsługuje wielopozycyjne koszyki, rozliczenia gotówkowe (COD), bezhasłowe logowanie OTP z blokadą równoległych sesji, zdarzeniowe powiadomienia e-mail oraz dynamiczne śledzenie pozycji kurierów w czasie rzeczywistym z wykorzystaniem Apache Kafka. Architektura rozdziela trwałe dane biznesowe (PostgreSQL) od wysokoczęstotliwościowych strumieni danych i asynchronicznego przetwarzania w tle (pamięć podręczna Redis oraz magistrala zdarzeń Apache Kafka).
</p>

<h2>Możliwości Systemu</h2>
<p>
  <b>Hybrydowa Architektura Zdarzeniowa:</b> Połączenie bazy PostgreSQL dla trwałych encji biznesowych, pamięci Redis dla ulotnej telemetrii/sesji oraz magistrali Apache Kafka do asynchronicznego, nieblokującego przetwarzania zdarzeń.<br>
  <b>Asynchroniczne Przetwarzanie Zdarzeń (Apache Kafka):</b> Odciążenie głównego wątku HTTP od wysyłki maili i czyszczenia pamięci podręcznej dzięki konsumentom w tle (<code>OrderEventsConsumer</code>, <code>CourierLocationConsumer</code>), co redukuje czas odpowiedzi REST API do ułamków milisekund.<br>
  <b>Śledzenie Geoprzestrzenne & WebSockets Live:</b> Rejestracja pozycji GPS w strukturze Redis Geo (<code>GEOADD</code>/<code>GEOPOS</code>) oraz bezpośrednia transmisja na żywo do klientów przez gniazda WebSocket.<br>
  <b>Ochrona Prywatności Pracowników:</b> Dostęp do pozycji GPS kuriera jest przyznawany klientowi wyłącznie w trakcie realizacji dostawy (status <code>ON_THE_WAY</code>).<br>
  <b>Zarządzanie Cyklem Życia i Analityka Czasu:</b> Obsługa pełnego procesu zamówienia (<code>PENDING</code> &rarr; <code>PREPARING</code> &rarr; <code>ON_THE_WAY</code> &rarr; <code>DELIVERED</code>) wraz z dynamicznym wyliczaniem czasu realizacji w minutach.<br>
  <b>Asynchroniczne Powiadomienia E-mail:</b> Powiadomienia na każdym etapie zamówienia (potwierdzenie złożenia, opłacenie, wyjazd kuriera, doręczenie z linkiem do oceny, anulowanie) wyzwalane w tle przez konsumentów Kafki.<br>
  <b>Rozliczanie Gotówki (Cash-on-Delivery):</b> Bilansowanie gotówki pobranej przez kuriera przy odbiorze (<code>PAY_BY_CASH</code>) na encji <code>Courier</code> oraz obsługa korekt balansu (<code>regulateBalance</code>) via JPA Dirty Checking.<br>
  <b>Czyszczenie Pamięci & Token Blacklisting:</b> Asynchroniczne usuwanie pozycji GPS z bazy Redis po doręczeniu lub anulowaniu zamówienia oraz unieważnianie tokenów JWT przy logowaniu z drugiego urządzenia lub wylogowaniu.<br>
  <b>Bezhasłowe Logowanie OTP i Single Active Session:</b> Autoryzacja kodami OTP (TTL 5 minut) z wymuszeniem pojedynczej aktywnej sesji dla kuriera.<br>
  <b>System Ocen Zamówień (Client Rate):</b> Pętla zwrotna z dedykowanym linkiem (<code>/rate</code>) w e-mailu zapobiegająca wielokrotnemu ocenianiu tej samej transakcji.<br>
  <b>Centralna Obsługa Błędów:</b> Przechwytywanie wyjątków domenowych i walidacji przez <code>@RestControllerAdvice</code> ze spójnym formatem błędów JSON.<br>
  <b>Gotowe Dane Startowe (DataInitializer):</b> Automatyczne zasilenie bazy danymi testowymi przy starcie aplikacji umożliwiające natychmiastowe testowanie w Postmanie.
</p>

<h2>Stos Technologiczny</h2>
<p>
  <b>Język i Framework:</b> Java 26 / Spring Boot 3.x (Web, Data JPA, Data Redis, Security, Validation, JavaMailSender, Spring Kafka)<br>
  <b>Architektura Zdarzeniowa:</b> Apache Kafka (Spring Kafka, Rekordy DTO Zdarzeń)<br>
  <b>Bazy Danych:</b> PostgreSQL 16 & Redis 7 (Sterownik Lettuce, StringRedisTemplate)<br>
  <b>Komunikacja Live:</b> Spring WebSocket (STOMP broker / SimpMessagingTemplate)<br>
  <b>Bezpieczeństwo:</b> Spring Security 6 (Bezstanowe JWT, Cookie HttpOnly, Custom JWTFilter)<br>
  <b>ORM i Narzędzia:</b> Hibernate 6/7 / JPA (UUID Primary Keys, Unidirectional One-To-Many), Lombok, Docker, Maven
</p>

<h2>Architektura i Rozwiązania Techniczne</h2>
<p>
  <b>1. Asynchroniczne Rozsprzęglenie Zdarzeniowe (OrderEvent & CourierLocationEvent)</b><br>
  Zdarzenia domenowe zostały zamknięte w niemutowalnych rekordach Javy (<code>OrderEvent</code>, <code>CourierLocationEvent</code>) z metodami fabrykującymi <code>.from()</code>. Wyeliminowało to błędy cyklicznej serializacji Jacksona oraz problemy z leniwym ładowaniem encji JPA podczas wysyłki do Kafki.
</p>

<p>
  <b>2. Telemetria w Pamięci RAM i Transmisja WebSocket</b><br>
  Endpoint <code>POST /api/db/couriers/location</code> natychmiast przekazuje koordynaty do topiku Kafki <code>courier-locations</code>. Konsument <code>CourierLocationConsumer</code> w tle zapisuje punkty do Redis Geo oraz transmituje sygnał na żywo przez WebSocket pod adres <code>/topic/courier/{id}/location</code>.
</p>

<p>
  <b>3. Nieblokujące Zarządzanie Cyklem Życia Zamówienia</b><br>
  Zdarzenia zmiany stanu zamówienia (<code>placeOrder</code>, <code>payOrder</code>, <code>orderOnTheWay</code>, <code>orderDelivered</code>, <code>orderCancelled</code>) aktualizują bazę PostgreSQL i publikują komunikat <code>OrderEvent</code> na topik <code>order-events</code>. Czasochłonne operacje (wysyłka maili SMTP, czyszczenie pozycji w Redisie) realizowane są asynchronicznie przez <code>OrderEventsConsumer</code>.
</p>

<p>
  <b>4. Kontekstowa Weryfikacja Dostępności GPS (trackOrder)</b><br>
  Zapytanie <code>GET /api/orders/{id}/track</code> sprawdza status zamówienia w PostgreSQL. Odpytanie komendą <code>GEOPOS</code> do Redisa następuje tylko wtedy, gdy zamówienie posiada status <code>ON_THE_WAY</code>.
</p>

<p>
  <b>5. Cykl Życia Dostawy, Anulowanie i Bilansowanie Portfela</b><br>
  Zmiana stanu zamówienia na <code>DELIVERED</code> wylicza czas realizacji, przypisuje pobraną gotówkę (<code>PAY_BY_CASH</code>) do balansu kuriera, wysyła zdarzenie do Kafki w celu wygenerowania wiadomości e-mail oraz zwalnia kuriera i usuwa jego pozycję z Redisa.
</p>

<h2>Instalacja i Konfiguracja</h2>
<p>
  <b>1. Wymagania Wstępne</b><br>
  Upewnij się, że w Twoim środowisku zainstalowane są: <b>JDK 26</b>, <b>Maven</b> oraz <b>Docker Desktop</b>.
</p>
<p>
  <b>2. Uruchomienie PostgreSQL, Redis i Kafka w Dockerze</b><br>
  Uruchom kontenery infrastrukturalne:
</p>
<pre><code>docker compose up -d</code></pre>
<p>
  <b>3. Uruchomienie Aplikacji</b><br>
  Skompiluj i uruchom aplikację Spring Boot przy użyciu Mavena:
</p>
<pre><code>mvn clean spring-boot:run</code></pre>
<p>
  Po pomyślnym uruchomieniu API będzie dostępne pod adresem <code>http://localhost:8080/api/</code> do testów w Postmanie.
</p>