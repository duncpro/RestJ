package com.duncpro.jrest;

import com.duncpro.jroute.Path;
import com.duncpro.jroute.route.Route;
import com.duncpro.jroute.route.WildcardRouteElement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ParameterizableRoute {
    private final Route abstractRoute;
    private final List<String> parameters;

    private ParameterizableRoute(Route abstractRoute, List<String> parameters) {
        this.abstractRoute = abstractRoute;
        this.parameters = List.copyOf(parameters);
    }

    public Route getAbstractRoute() { return this.abstractRoute; }

    public String getVariableElementValue(Path path, String pathParam) {
        final var parameterPosition = parameters.indexOf(pathParam);
        if (parameterPosition == -1) throw new IllegalArgumentException();
        final var elementPosition = getVariableElementIndexes().get(parameterPosition);
        return path.getElements().get(elementPosition);
    }

    private List<Integer> getVariableElementIndexes() {
        final var indexes = new ArrayList<Integer>();
        for (int i = 0; i < getAbstractRoute().getElements().size(); i++) {
            final var element = getAbstractRoute().getElements().get(i);
            if (element instanceof WildcardRouteElement) {
                indexes.add(i);
            }
        }
        return List.copyOf(indexes);
    }

    public ParameterizableRoute resolve(ParameterizableRoute suffix) {
        final var concatenatedParameters = new ArrayList<String>();
        concatenatedParameters.addAll(this.parameters);
        concatenatedParameters.addAll(suffix.parameters);
        final var concatenatedAbstractRoute = this.abstractRoute.resolve(suffix.abstractRoute);
        return new ParameterizableRoute(concatenatedAbstractRoute, concatenatedParameters);
    }

    static ParameterizableRoute parse(String parameterizedRouteString) {
        String wildcardRouteString = parameterizedRouteString;
        final var params = new ArrayList<String>();

        int openBraceIndex = -1;
        int closeBraceIndex = -1;

        for (int i = 0; i < parameterizedRouteString.length(); i++) {
            final var c = parameterizedRouteString.charAt(i);

            switch (c) {
                case '{':
                    openBraceIndex = i;
                    break;
                case '}':
                    closeBraceIndex = i;
                    break;
            }

            if (c == '}') {
                final var parameterString = parameterizedRouteString.substring(openBraceIndex, closeBraceIndex + 1);
                wildcardRouteString = wildcardRouteString.replaceFirst(Pattern.quote(parameterString), "*");
                params.add(parameterString.substring(1, parameterString.length() - 1));
            }
        }

        return new ParameterizableRoute(new Route(wildcardRouteString), params);
    }

    static final ParameterizableRoute ROOT = new ParameterizableRoute(Route.ROOT, List.of());
}
