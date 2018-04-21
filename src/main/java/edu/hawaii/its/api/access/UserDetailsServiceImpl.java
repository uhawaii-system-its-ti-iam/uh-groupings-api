package edu.hawaii.its.api.access;

//public class UserDetailsServiceImpl extends AbstractCasAssertionUserDetailsService {
//
//    private static final Log logger = LogFactory.getLog(UserDetailsServiceImpl.class);
//
//    private UserBuilder userBuilder;
//
//    public UserDetailsServiceImpl(UserBuilder userBuilder) {
//        super();
//        this.userBuilder = userBuilder;
//    }
//
//    @Override
//    protected UserDetails loadUserDetails(Assertion assertion) {
//        if (assertion.getPrincipal() == null) {
//            // Not sure this is possible.
//            throw new UsernameNotFoundException("principal is null");
//        }
//
//        Map<String, Object> map = assertion.getPrincipal().getAttributes();
//        logger.info("map: " + map);
//
//        return userBuilder.make(new UhCasAttributes(map));
//    }
//
//}
