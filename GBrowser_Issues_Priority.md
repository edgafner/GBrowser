# GBrowser Open Issues - Priority and Complexity Analysis

## Critical Priority - IDE Stability Issues

These issues affect the core functionality and stability of the IDE.

### 1. [#417](https://github.com/edgafner/GBrowser/issues/417) - IDEA Freezing when opening GitLab review requests

- **Priority**: Critical
- **Difficulty**: High
- **Impact**: IDE becomes completely unresponsive
- **Notes**: Affects GitLab users specifically, requires deep debugging of JCEF interaction

### 2. [#398](https://github.com/edgafner/GBrowser/issues/398) - IDEA cannot exit after using GBrowser

- **Priority**: Critical
- **Difficulty**: High
- **Impact**: IDE fails to shut down properly
- **Notes**: May be related to JCEF lifecycle management

### 3. [#284](https://github.com/edgafner/GBrowser/issues/284) - Plugin hangs IDEA when working with Markdown files

- **Priority**: Critical
- **Difficulty**: Medium-High
- **Impact**: IDE freezes during real-time markdown preview
- **Notes**: Possibly related to refresh/reload handling

### 4. [#287](https://github.com/edgafner/GBrowser/issues/287) - Browser autofocusing and IDE stuck (NextJS dev)

- **Priority**: Critical
- **Difficulty**: Medium
- **Impact**: Development workflow interruption and IDE freezing
- **Notes**: Focus management issue with hot reload

### 5. [#277](https://github.com/edgafner/GBrowser/issues/277) - Cursor gets stuck in text fields

- **Priority**: Critical
- **Difficulty**: Medium
- **Impact**: Makes plugin unusable on certain websites
- **Notes**: Focus/input handling issue in JCEF

## High Priority - Functional Bugs

### 6. [#418](https://github.com/edgafner/GBrowser/issues/418) - Not saving login information (cookies)

- **Priority**: High
- **Difficulty**: Medium
- **Impact**: User has to re-login after every IDE restart
- **Notes**: Cookie persistence issue in JCEF

### 7. [#190](https://github.com/edgafner/GBrowser/issues/190) - Cloudflare check loop on chat.openai.com

- **Priority**: High
- **Difficulty**: High
- **Impact**: Cannot access certain websites
- **Notes**: May require user agent or browser detection fixes

### 8. [#326](https://github.com/edgafner/GBrowser/issues/326) - Unpinned mode occasionally not showing

- **Priority**: Medium-High
- **Difficulty**: Low-Medium
- **Impact**: UI visibility issue
- **Notes**: Tool window state management

### 9. [#181](https://github.com/edgafner/GBrowser/issues/181) - Debug port not working

- **Priority**: Medium-High
- **Difficulty**: Medium
- **Impact**: Cannot use Chrome DevTools externally
- **Notes**: Port binding configuration issue

## Medium Priority - Feature Enhancements

### 10. [#446](https://github.com/edgafner/GBrowser/issues/446) - Dark mode for web pages ✅

- **Priority**: Medium
- **Difficulty**: Low
- **Status**: Already implemented in current session
- **Notes**: Theme switching functionality added

### 11. [#288](https://github.com/edgafner/GBrowser/issues/288) - Mobile device emulation ✅

- **Priority**: Medium
- **Difficulty**: Medium
- **Status**: Already implemented in current session
- **Notes**: Device emulation with various profiles added

### 12. [#192](https://github.com/edgafner/GBrowser/issues/192) - Allow localhost SSL via chrome://flags

- **Priority**: Medium
- **Difficulty**: High
- **Impact**: Development with local SSL certificates
- **Notes**: Requires CEF flags configuration

### 13. [#191](https://github.com/edgafner/GBrowser/issues/191) - Icon consistent with new JB look

- **Priority**: Low-Medium
- **Difficulty**: Low
- **Impact**: Visual consistency
- **Notes**: Simple icon update

## Low Priority - Enhancements

### 14. [#285](https://github.com/edgafner/GBrowser/issues/285) - Treat GBrowser tabs like source file tabs

- **Priority**: Low
- **Difficulty**: Very High
- **Impact**: Workflow improvement
- **Notes**: Would require significant architectural changes

### 15. [#49](https://github.com/edgafner/GBrowser/issues/49) - Open current file in browser

- **Priority**: Low
- **Difficulty**: Low
- **Impact**: Convenience feature
- **Notes**: Simple action implementation

## Other

### 16. [#103](https://github.com/edgafner/GBrowser/issues/103) - Sponsorship request

- Not a technical issue

## Summary

**Immediate Focus Areas:**

1. IDE stability issues (#417, #398, #284, #287, #277) - These are critical and affect user productivity
2. Cookie persistence (#418) - Important for user experience
3. Cloudflare/website compatibility (#190) - Blocks access to popular sites

**Quick Wins:**

- Icon update (#191)
- Open file in browser (#49)

**Already Completed:**

- ✅ Dark mode support (#446)
- ✅ Mobile device emulation (#288)

**Complex Long-term:**

- Tab management redesign (#285)
- Chrome flags support (#192)