package org.springframework.servlet.mvc.view.jmustache;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;
import com.samskivert.mustache.Template;

/**
 * See https://gist.github.com/1603296
 *
 * @author Keith Donald
 *
 */
public class JMustacheViewResolver extends AbstractTemplateViewResolver
        implements ViewResolver {

    private TemplateLoader templateLoader;

    private Compiler compiler;

    public JMustacheViewResolver(ResourceLoader resourceLoader) {
        setViewClass(JMustacheView.class);
        setExposeSpringMacroHelpers(false);
        setSuffix(".html");
        initJMustache(resourceLoader);
    }

    @Override
    protected Class<?> requiredViewClass() {
        return JMustacheView.class;
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        JMustacheView view = (JMustacheView) super.buildView(viewName);
        Template template = compiler.compile(templateLoader.getTemplate(view.getUrl()));
        view.setTemplate(template);
        return view;
    }

    private void initJMustache(ResourceLoader resourceLoader) {
        templateLoader = new ResourceTemplateLoader(resourceLoader);
        compiler = Mustache.compiler().nullValue("").withLoader(templateLoader);
    }

    private static class ResourceTemplateLoader implements TemplateLoader {

        private static final String DEFAULT_ENCODING = "UTF-8";

        private final ResourceLoader resourceLoader;

        private String encoding = DEFAULT_ENCODING;

        public ResourceTemplateLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public Reader getTemplate(String name) throws Exception {
            return new InputStreamReader(resourceLoader.getResource(name).getInputStream(), encoding);
        }

    }

    private static class JMustacheView extends AbstractTemplateView {

        private Template template;

        public void setTemplate(Template template) {
            this.template = template;
        }

        @Override
        protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            response.setContentType(getContentType());
            Writer writer = response.getWriter();
            try {
                template.execute(model, writer);
            } finally {
                writer.flush();
            }
        }

    }

}