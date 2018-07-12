package microsec.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("catalog-branding")
public class CatalogBranding {
    private String companyName;
    private String catalogTitle;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCatalogTitle() {
        return catalogTitle;
    }

    public void setCatalogTitle(String catalogTitle) {
        this.catalogTitle = catalogTitle;
    }
}
