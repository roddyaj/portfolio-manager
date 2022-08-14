function TitleBar(props) {
	const { accounts, setSelectedAccount } = props;

	const accountOptions = accounts.map((account) => {
		return (<option key={account} value={account}>{account}</option>);
	});

	return (
		<div className="pm-block">
			<div style={{ display: "flex" }}>
				<div className="pm-main-header-item"></div>
				<div className="pm-main-header-item title" style={{ textAlign: "center" }}>
					<select name="accounts" id="accounts" onChange={(e) => setSelectedAccount(e.target.value)}>
						{accountOptions}
					</select>
				</div>
				<div className="pm-main-header-item" style={{ textAlign: "right" }}></div>
			</div>
		</div>
	);
}

export default TitleBar;
