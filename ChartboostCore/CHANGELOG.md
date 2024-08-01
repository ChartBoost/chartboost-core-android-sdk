Android Change Log
==================

### Version 1.0.0 *(2024-08-01)*
Improvements:
- Initialization now takes an `SdkConfiguration` object and an optional `ModuleObserver`.
- Renamed `ConsentKeys` and `ConsentValues` and changed them to a list of constants instead of an enum for ease of use.
- Added automatic shared preference getter for IAB defaults to `ConsentAdapter`.
- See the [documentation](https://docs.chartboost.com/en/mediation/integrate/core/android/get-started/) for more information.

### Version 0.4.0 *(2023-12-07)*
Improvements:
- Added `partnerConsentStatus: Map<String, ConsentStatus>` to `ConsentManagementPlatform. This is to facilitate per-partner consent for Mediation.
- Added `fun onPartnerConsentStatusChange(partnerId: String, status: ConsentStatus)` to `ConsentObserver`.
- Added ability for Core to automatically subscribe `PublisherMetadataObserver` modules.

### Version 0.3.0 *(2023-10-12)*
Improvements:
- Added `PublisherMetadataObserver`s to receive notifications that publisher metadata has changed.

Bug Fixes:
- Made several environment getters async to ensure reliability.

### Version 0.2.0 *(2023-8-29)*
Improvements:
- Split out Usercentrics adapter.
- Implemented most of the minor features.

### Version 0.1.0 *(2023-8-23)*
Improvements:
- Initial version with basic functionality.
