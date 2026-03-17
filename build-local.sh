#!/bin/bash
# Helper script for convenient local GraphHopper + ORS development
# 
# Usage:
#   ./build-local.sh              # Build with auto-rebuild of GraphHopper
#   ./build-local.sh fast         # Use pre-built GraphHopper (fast ORS rebuild)
#   ./build-local.sh gh-only      # Build only GraphHopper modules
#   ./build-local.sh help         # Show all options

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}===================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}===================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

show_help() {
    cat << EOF
${BLUE}OpenRouteService Local GraphHopper Helper${NC}

${YELLOW}USAGE:${NC}
  ./build-local.sh [command] [options]

${YELLOW}COMMANDS:${NC}
  (default)        Build ORS with automatic GraphHopper rebuild
  fast             Use pre-built local GraphHopper (fast ORS rebuild only)
  gh-only          Build only GraphHopper modules (no ORS)
  compile          Compile ORS (fast, no package/test)
  test             Run tests against pre-built GraphHopper
  verify           Full verification build (compile + test + package)
  help             Show this help message

${YELLOW}OPTIONS:${NC}
  --skip-tests     Skip test execution (for default, fast, verify)
  --modules=M1,M2  Specify GraphHopper modules (default: core,web-api,reader-gtfs,map-matching)
  --verbose        Show detailed Maven output (-X flag)

${YELLOW}EXAMPLES:${NC}
  # Build everything with auto GraphHopper rebuild
  ./build-local.sh

  # Fast ORS rebuild only (using pre-built GraphHopper)
  ./build-local.sh fast

  # Compile only, no tests
  ./build-local.sh compile --skip-tests

  # Run tests
  ./build-local.sh test

  # Build only GraphHopper core and web-api
  ./build-local.sh --modules=core,web-api gh-only

  # Full build with verbose output
  ./build-local.sh --verbose

${YELLOW}VERIFICATION:${NC}
  After building, verify GraphHopper artifacts:
    ls -la ~/.m2/repository/com/github/GIScience/graphhopper/graphhopper-core/v4.12.0/

  Check dependency tree:
    mvn dependency:tree -Dincludes=com.github.GIScience.graphhopper
EOF
}

# Default values
COMMAND="${1:-default}"
SKIP_TESTS=""
MODULES="core,web-api,reader-gtfs,map-matching"
VERBOSE=""

# Parse options
shift 2>/dev/null || true
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS="-DskipTests"
            ;;
        --modules=*)
            MODULES="${1#--modules=}"
            ;;
        --verbose)
            VERBOSE="-X"
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
    shift
done

# Execute command
case "$COMMAND" in
    help)
        show_help
        ;;
    default)
        print_header "Building ORS with auto-rebuild of local GraphHopper"
        print_info "Building GraphHopper modules: $MODULES"
        mvn -DlocalGraphhopper=true -DlocalGraphhopperModules="$MODULES" $SKIP_TESTS $VERBOSE clean package
        print_success "Build completed!"
        ;;
    fast)
        print_header "Fast ORS rebuild (using pre-built GraphHopper)"
        print_info "Skipping GraphHopper rebuild - using artifacts from ~/.m2/repository"
        mvn -DlocalGraphhopperOnly=true $SKIP_TESTS $VERBOSE clean package
        print_success "Fast build completed!"
        ;;
    compile)
        print_header "Compiling ORS (no tests, no package)"
        mvn -DlocalGraphhopperOnly=true -DskipTests $VERBOSE clean compile
        print_success "Compile completed!"
        ;;
    test)
        print_header "Running ORS tests"
        mvn -DlocalGraphhopperOnly=true $VERBOSE clean test
        print_success "Tests completed!"
        ;;
    verify)
        print_header "Full verification build (compile + test + package)"
        mvn -DlocalGraphhopperOnly=true $VERBOSE clean verify
        print_success "Verification completed!"
        ;;
    gh-only)
        print_header "Building only GraphHopper modules"
        print_info "Building GraphHopper modules: $MODULES"
        cd ../graphhopper
        mvn $SKIP_TESTS $VERBOSE clean install -pl "$MODULES" -am
        cd - > /dev/null
        print_success "GraphHopper build completed!"
        ;;
    *)
        print_error "Unknown command: $COMMAND"
        show_help
        exit 1
        ;;
esac
