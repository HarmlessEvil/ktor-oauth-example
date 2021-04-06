# OAuth Test Task

## Prerequisites

1. Put appropriate Google credentials file to 
   `src/main/resources/client_secret.json`
2. Set environment variables `ENCRYPT_KEY` and `AUTH_KEY` to
   appropriate values. It enables cookies encryption for storing
   users auth tokens from Google services.
   
## Notes

* Auth tokens are stored in memory, so it will not be available
    after a restart of server.
* Auth tokens encrypted with `AES` for encryption and 
  `HmacSHA256` for authentication. 
  [Read more](https://ktor.io/docs/transformers.html#SessionTransportTransformerEncrypt)
* Auth tokens just getting forgotten on logout, not revoked. 
  [[1]](https://community.ory.sh/t/should-i-revoke-the-access-token-on-logout-from-client/2120)

## Improvements

* Move user fetching to some kind of abstraction like repository. 
  This way I'd be able to mock the repository to achieve better
  test coverage.
